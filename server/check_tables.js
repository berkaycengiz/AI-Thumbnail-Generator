require('dotenv').config();
const { createClient } = require('@supabase/supabase-js');

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE || process.env.SUPABASE_KEY);

async function run() {
    console.log("Attempting relational join query...");
    const { data, error } = await supabase
        .from('thumbnails')
        .select('*, profiles(display_name, avatar_url)')
        .order('created_at', { ascending: false })
        .limit(2);

    if (error) {
        console.error("Supabase Error:", error);
    } else {
        console.log("Successfully joined! Rows found:", data.length);
        if (data.length > 0) {
            console.log("Joined Row Data:", JSON.stringify(data[0], null, 2));
        }
    }
}

run();
