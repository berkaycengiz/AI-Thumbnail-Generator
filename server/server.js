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
    const { title, ratio, socketId, userId, type } = req.body;

    if (!title || !ratio || !socketId || !userId) {
        return res.status(400).json({ error: 'Missing required fields' });
    }

    try {
        let visualPrompt = title;
        let strategy = { styleType: 'Art', hookText: '', colorPalette: '' };

        if (type !== 'art') {
            const orResponse = await axios.post('https://openrouter.ai/api/v1/chat/completions', {
                model: "openrouter/auto",
                messages: [{
                    role: "user",
                    content: `Act as a social media expert. Create a thumbnail strategy for a video titled: '${title}'. Return a JSON with: styleType, hookText, colorPalette (hex), visualPrompt.`
                }],
                response_format: { type: "json_object" }
            }, {
                headers: { 'Authorization': `Bearer ${process.env.OPENROUTER_KEY}` }
            });

            strategy = JSON.parse(orResponse.data.choices[0].message.content);
            visualPrompt = strategy.visualPrompt;
        }

        let w = 1280, h = 720;
        if (ratio === '1:1') { w = 1024; h = 1024; }
        else if (ratio === '9:16') { w = 768; h = 1344; }

        const leoResponse = await axios.post('https://cloud.leonardo.ai/api/rest/v2/generations', {
            model: "gpt-image-2",
            public: false,
            parameters: {
                prompt: visualPrompt,
                width: 1024,
                height: 1024,
                quality: "LOW",
                prompt_enhance: "OFF"
            }
        }, {
            headers: {
                'Authorization': `Bearer ${process.env.LEONARDO_KEY}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        console.log('Leonardo Full Response:', JSON.stringify(leoResponse.data, null, 2));

        // ID'yi yakalamak için loglardaki gerçek yolu da ekliyoruz
        const genId = leoResponse.data.generate?.generationId ||
            leoResponse.data.id ||
            (leoResponse.data.sdGenerationJob && leoResponse.data.sdGenerationJob.generationId);

        console.log('Generation started, ID:', genId);

        activeJobs.set(genId, { socketId, strategy, title, ratio, userId });

        io.to(socketId).emit('status_update', { message: 'AI is painting (Waiting for webhook)...' });

        res.json({ success: true, generationId: genId });

    } catch (error) {
        console.error('Server Error:', error.response?.data || error.message);
        res.status(500).json({ error: 'Failed to start generation' });
    }
});

app.post('/api/webhook/leonardo', async (req, res) => {
    console.log('--- WEBHOOK RECEIVED ---');
    const payload = req.body;

    // Webhook logundaki yapıya (data.object) göre okuyoruz
    const genData = payload.data?.object || payload.data || payload;
    const genId = payload.id || genData.id || genData.generationId;
    const status = genData.status;

    console.log(`Checking job for ID: ${genId}, Status: ${status}`);

    const job = activeJobs.get(genId);
    if (job) {
        console.log(`Match found for job! Status is: ${status}`);
        if (status === 'COMPLETE') {
            const imageUrl = genData.generated_images[0].url;
            console.log('Image URL found:', imageUrl);

            const { error } = await supabase.from('thumbnails').insert([{
                user_id: job.userId,
                original_title: job.title,
                hook_text: job.strategy.hookText,
                color_palette: job.strategy.colorPalette,
                image_url: imageUrl,
                ratio_type: job.ratio,
                is_public: false
            }]);

            if (error) {
                console.error('SUPABASE INSERT ERROR:', error.message);
            } else {
                console.log('Successfully saved to Supabase!');
                io.to(job.socketId).emit('thumbnail_ready', {
                    imageUrl,
                    strategy: job.strategy,
                    title: job.title,
                    ratio: job.ratio
                });
            }

            activeJobs.delete(genId);
        }
    } else {
        console.warn(`No active job found for ID: ${genId}. (Maybe server restarted?)`);
    }
    res.sendStatus(200);
});

const PORT = process.env.PORT || 8080;
server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
