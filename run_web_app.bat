@echo off
echo [1/3] Starting Backend...
start cmd /k "cd backend && npm run dev"

echo [2/3] Starting Frontend (Web)...
start cmd /k "cd app && npm run dev"

echo [3/3] Frontend is running at http://localhost:5173
echo You can view the app in your browser immediately.
echo For native testing, use:
echo cd app
echo npx cap run android (or ios)
pause
