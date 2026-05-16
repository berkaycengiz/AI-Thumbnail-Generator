require('dotenv').config();
const express = require('express');
const axios = require('axios');
const cors = require('cors');
const http = require('http');
const { Server } = require('socket.io');
const { createClient } = require('@supabase/supabase-js');

const app = express();
app.use(cors());
app.use(express.json());

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_KEY);

const server = http.createServer(app);
const io = new Server(server, {
    cors: { origin: "*", methods: ["GET", "POST"] }
});

const activeJobs = new Map();

io.on('connection', (socket) => {
    console.log('Client connected:', socket.id);
});

app.get('/api/history', async (req, res) => {
    const { data, error } = await supabase
        .from('thumbnails')
        .select('*')
        .order('created_at', { ascending: false });

    if (error) return res.status(500).json({ error: error.message });
    res.json(data);
});

app.post('/api/generate', async (req, res) => {
    const { title, ratio, socketId, userId } = req.body;
    
    if (!title || !ratio || !socketId || !userId) {
        return res.status(400).json({ error: 'Missing required fields: title, ratio, socketId, userId' });
    }

    try {
        const orResponse = await axios.post('https://openrouter.ai/api/v1/chat/completions', {
            model: "meta-llama/llama-3-8b-instruct:free",
            messages: [{
                role: "user",
                content: `Act as a social media expert. Create a thumbnail strategy for a video titled: '${title}'. Return a JSON with: styleType, hookText, colorPalette (hex), visualPrompt.`
            }],
            response_format: { type: "json_object" }
        }, {
            headers: { 'Authorization': `Bearer ${process.env.OPENROUTER_KEY}` }
        });

        const strategy = JSON.parse(orResponse.data.choices[0].message.content);
        
        let w = 1280, h = 720;
        if (ratio === '1:1') { w = 1024; h = 1024; }
        else if (ratio === '9:16') { w = 768; h = 1344; }

        const leoResponse = await axios.post('https://cloud.leonardo.ai/api/rest/v1/generations', {
            prompt: strategy.visualPrompt,
            modelId: "6bef9f1b-29cb-40c7-b9df-cd99d9ff2034",
            width: w,
            height: h,
            num_images: 1,
            webhookUrl: `${process.env.WEBHOOK_URL}/api/webhook/leonardo`
        }, {
            headers: { 'Authorization': `Bearer ${process.env.LEONARDO_KEY}` }
        });

        const genId = leoResponse.data.sdGenerationJob.generationId;
        activeJobs.set(genId, { socketId, strategy, title, ratio, userId });
        
        io.to(socketId).emit('status_update', { message: 'AI is painting (Waiting for webhook)...' });

        res.json({ success: true, generationId: genId });

    } catch (error) {
        console.error('Server Error:', error.response?.data || error.message);
        res.status(500).json({ error: 'Failed to start generation' });
    }
});

app.post('/api/webhook/leonardo', async (req, res) => {
    const payload = req.body;
    const genId = payload.id || payload.generationId;
    const status = payload.status;

    if (status === 'COMPLETE' && activeJobs.has(genId)) {
        const job = activeJobs.get(genId);
        const imageUrl = payload.generated_images?.[0]?.url;

        if (imageUrl) {
            const { error } = await supabase
                .from('thumbnails')
                .insert([{
                    user_id: job.userId,
                    original_title: job.title,
                    hook_text: job.strategy.hookText,
                    color_palette: job.strategy.colorPalette,
                    image_url: imageUrl,
                    ratio_type: job.ratio,
                    is_public: false
                }]);

            if (error) console.error('Supabase Insert Error:', error.message);

            io.to(job.socketId).emit('thumbnail_ready', {
                imageUrl,
                strategy: job.strategy,
                title: job.title,
                ratio: job.ratio
            });
            activeJobs.delete(genId);
        }
    }
    res.sendStatus(200);
});

const PORT = process.env.PORT || 8080;
server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
