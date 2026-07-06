const fs = require('fs');
const path = require('path');

function buscarYParchar(dir) {
  if (!fs.existsSync(dir)) return;
  const archivos = fs.readdirSync(dir);
  for (const archivo of archivos) {
    const rutaCompleta = path.join(dir, archivo);
    try {
      const stat = fs.statSync(rutaCompleta);
      if (stat.isDirectory()) {
        if (archivo === '.bin') continue;
        if (archivo === 'metro-cache') {
          const pkgPath = path.join(rutaCompleta, 'package.json');
          if (fs.existsSync(pkgPath)) {
            const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
            if (pkg.exports && !pkg.exports['./src/stores/FileStore']) {
              pkg.exports['./src/stores/FileStore'] = './src/stores/FileStore.js';
              fs.writeFileSync(pkgPath, JSON.stringify(pkg, null, 2), 'utf8');
              console.log(`[Parchado] -> ${pkgPath}`);
            }
          }
        }
        buscarYParchar(rutaCompleta);
      }
    } catch (e) {
      console.error('Error:', e.message);
    }
  }
}

const nodoRaiz = path.join(__dirname, 'node_modules');
if (fs.existsSync(nodoRaiz)) {
  buscarYParchar(nodoRaiz);
}

