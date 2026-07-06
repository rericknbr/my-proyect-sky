const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);

// Forzar a Metro a usar caché en memoria para evitar el bug de exportaciones de Node
config.cacheStores = [];

module.exports = config;
