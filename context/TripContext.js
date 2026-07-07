import AsyncStorage from "@react-native-async-storage/async-storage";
import React, { createContext, useCallback, useContext, useEffect, useState, } from "react";
const DEFAULT_CONFIG = { limitGreen: 8.0, limitYellow: 6.0 };
const CONFIG_KEY = "@viaje_rentable:config";
const HISTORY_KEY = "@viaje_rentable:history";
const TripContext = createContext(null);
export function TripProvider({ children }) {
    const [config, setConfig] = useState(DEFAULT_CONFIG);
    const [history, setHistory] = useState([]);
    useEffect(() => {
        (async () => {
            try {
                const [storedConfig, storedHistory] = await Promise.all([
                    AsyncStorage.getItem(CONFIG_KEY),
                    AsyncStorage.getItem(HISTORY_KEY),
                ]);
                if (storedConfig)
                    setConfig(JSON.parse(storedConfig));
                if (storedHistory)
                    setHistory(JSON.parse(storedHistory));
            }
            catch { }
        })();
    }, []);
    const updateConfig = useCallback(async (green, yellow) => {
        const newConfig = { limitGreen: green, limitYellow: yellow };
        setConfig(newConfig);
        await AsyncStorage.setItem(CONFIG_KEY, JSON.stringify(newConfig));
    }, []);
    const getStatus = useCallback((earningsPerKmReal) => {
        if (earningsPerKmReal >= config.limitGreen)
            return "EXCELENTE";
        if (earningsPerKmReal >= config.limitYellow)
            return "ACEPTABLE";
        return "MALO";
    }, [config]);
    const calculateTrip = useCallback((earnings, pickupDistance, pickupUnit, tripDistance, platform) => {
        const pickupKm = pickupUnit === "m" ? pickupDistance / 1000 : pickupDistance;
        const totalDistance = pickupKm + tripDistance;
        const earningsPerKmReal = totalDistance > 0 ? earnings / totalDistance : 0;
        const earningsPerKmTrip = tripDistance > 0 ? earnings / tripDistance : 0;
        const status = earningsPerKmReal >= config.limitGreen
            ? "EXCELENTE"
            : earningsPerKmReal >= config.limitYellow
                ? "ACEPTABLE"
                : "MALO";
        return {
            platform,
            earnings,
            pickupDistance,
            pickupUnit,
            tripDistance,
            totalDistance,
            earningsPerKmReal,
            earningsPerKmTrip,
            status,
        };
    }, [config]);
    const addToHistory = useCallback(async (result) => {
        const item = {
            id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
            timestamp: Date.now(),
            ...result,
        };
        const newHistory = [item, ...history];
        setHistory(newHistory);
        await AsyncStorage.setItem(HISTORY_KEY, JSON.stringify(newHistory));
    }, [history]);
    const deleteHistoryItem = useCallback(async (id) => {
        const newHistory = history.filter((h) => h.id !== id);
        setHistory(newHistory);
        await AsyncStorage.setItem(HISTORY_KEY, JSON.stringify(newHistory));
    }, [history]);
    const clearHistory = useCallback(async () => {
        setHistory([]);
        await AsyncStorage.setItem(HISTORY_KEY, JSON.stringify([]));
    }, []);
    return (<TripContext.Provider value={{
            config,
            history,
            updateConfig,
            addToHistory,
            deleteHistoryItem,
            clearHistory,
            calculateTrip,
            getStatus,
        }}>
      {children}
    </TripContext.Provider>);
}
export function useTripContext() {
    const ctx = useContext(TripContext);
    if (!ctx)
        throw new Error("useTripContext must be used within TripProvider");
    return ctx;
}
