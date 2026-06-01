require('dotenv').config();
const { createClient } = require('@supabase/supabase-js');

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE || process.env.SUPABASE_KEY);

async function run() {
    const { data, error } = await supabase
        .from('thumbnails')
        .select('*')
        .limit(1);

    if (error) {
        console.error("Supabase Error:", error);
    } else {
        if (data.length > 0) {
            console.log("Available columns in 'thumbnails':", Object.keys(data[0]));
            console.log("Full row sample data:", data[0]);
        } else {
            console.log("No rows found in table.");
        }
    }
}

run();
