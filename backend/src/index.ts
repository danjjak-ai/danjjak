import express from 'express';
import { json } from 'body-parser';
import adviceRoutes from './routes/advice.routes.js';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(json());

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'OK', message: 'Danjjak Backend is running' });
});

app.use('/api', adviceRoutes);

app.listen(PORT, () => {
    console.log(`[Danjjak] Server is running on port ${PORT}`);
});
