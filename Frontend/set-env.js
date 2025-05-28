const fs = require('fs');
const path = require('path');

const envFilePath = path.join(__dirname, 'src/environments/environment.ts');

const apiUrl = process.env.API_URL;
const websocketUrl = process.env.WEBSOCKET_URL;

const envFileContent = `export const environment = {
  production: false,
  apiUrl: '${apiUrl}',
  websocketUrl: '${websocketUrl}'
};`;

fs.writeFileSync(envFilePath, envFileContent);

console.log('✔ environment.ts wurde erfolgreich generiert.');
