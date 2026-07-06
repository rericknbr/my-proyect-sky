import AsyncStorage from "@react-native-async-storage/async-storage";
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";

export interface AppConfig {
  limitGreen: number;
  limitYellow: number;
}

export interface TripHistory {
  id: string;
  timestamp: number;
  platform: string;
  earnings: number;
  pickupDistance: number;
  pickupUnit: string;
  tripDistance: number;
  totalDistance: number;
  earningsPerKmReal: number;
  earningsPerKmTrip: number;
  status: string;
}

export interface CalculationResult {
  platform: string;
  earnings: number;
  pickupDistance: number;
  pickupUnit: string;
  tripDistance: number;
  totalDistance: number;
  earningsPerKmReal: number;
  earningsPerKmTrip: number;
  status: string;
}

interface TripContextValue {
  config: AppConfig;
  history: TripHistory[];
  updateConfig: (green: number, yellow: number) => Promise<void>;
  addToHistory: (result: CalculationResult) => Promise<void>;
  deleteHistoryItem: (id: string) => Promise<void>;
  clearHistory: () => Promise<void>;
  calculateTrip: (
    earnings: number,
    pickupDistance: number,
    pickupUnit: string,
    tripDistance: number,
    platform: string
  ) => CalculationResult;
  getStatus: (earningsPerKmReal: number) => string;
}

const DEFAULT_CONFIG: AppConfig = { limitGreen: 8.0, limitYellow: 6.0 };

const CONFIG_KEY = "@viaje_rentable:config";
const HISTORY_KEY = "@viaje_rentable:history";

const TripContext = createContext<TripContextValue | null>(null);

export function TripProvider({ children }: { children: React.ReactNode }) {
  const [config, setConfig] = useState<AppConfig>(DEFAULT_CONFIG);
  const [history, setHistory] = useState<TripHistory[]>([]);

  useEffect(() => {
    (async () => {
      try {
        const [storedConfig, storedHistory] = await Promise.all([
          AsyncStorage.getItem(CONFIG_KEY),
          AsyncStorage.getItem(HISTORY_KEY),
        ]);
        if (storedConfig) setConfig(JSON.parse(storedConfig));
        if (storedHistory) setHistory(JSON.parse(storedHistory));
      } catch {}
    })();
  }, []);

  const updateConfig = useCallback(async (green: number, yellow: number) => {
    const newConfig: AppConfig = { limitGreen: green, limitYellow: yellow };
    setConfig(newConfig);
    await AsyncStorage.setItem(CONFIG_KEY, JSON.stringify(newConfig));
  }, []);

  const getStatus = useCallback(
    (earningsPerKmReal: number): string => {
      if (earningsPerKmReal >= config.limitGreen) return "EXCELENTE";
      if (earningsPerKmReal >= config.limitYellow) return "ACEPTABLE";
      return "MALO";
    },
    [config]
  );

  const calculateTrip = useCallback(
    (
      earnings: number,
      pickupDistance: number,
      pickupUnit: string,
      tripDistance: number,
      platform: string
    ): CalculationResult => {
      const pickupKm = pickupUnit === "m" ? pickupDistance / 1000 : pickupDistance;
      const totalDistance = pickupKm + tripDistance;
      const earningsPerKmReal = totalDistance > 0 ? earnings / totalDistance : 0;
      const earningsPerKmTrip = tripDistance > 0 ? earnings / tripDistance : 0;
      const status =
        earningsPerKmReal >= config.limitGreen
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
    },
    [config]
  );

  const addToHistory = useCallback(
    async (result: CalculationResult) => {
      const item: TripHistory = {
        id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
        timestamp: Date.now(),
        ...result,
      };
      const newHistory = [item, ...history];
      setHistory(newHistory);
      await AsyncStorage.setItem(HISTORY_KEY, JSON.stringify(newHistory));
    },
    [history]
  );

  const deleteHistoryItem = useCallback(
    async (id: string) => {
      const newHistory = history.filter((h) => h.id !== id);
      setHistory(newHistory);
      await AsyncStorage.setItem(HISTORY_KEY, JSON.stringify(newHistory));
    },
    [history]
  );

  const clearHistory = useCallback(async () => {
    setHistory([]);
    await AsyncStorage.setItem(HISTORY_KEY, JSON.stringify([]));
  }, []);

  return (
    <TripContext.Provider
      value={{
        config,
        history,
        updateConfig,
        addToHistory,
        deleteHistoryItem,
        clearHistory,
        calculateTrip,
        getStatus,
      }}
    >
      {children}
    </TripContext.Provider>
  );
}

export function useTripContext() {
  const ctx = useContext(TripContext);
  if (!ctx) throw new Error("useTripContext must be used within TripProvider");
  return ctx;
}
