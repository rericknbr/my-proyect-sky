import React from "react";
import { StyleSheet, Text, View } from "react-native";
import { useColors } from "@/hooks/useColors";
import { StatusBadge } from "./StatusBadge";
export function ResultCard({ result }) {
    const colors = useColors();
    const statusColor = result.status === "EXCELENTE"
        ? colors.semGreen
        : result.status === "ACEPTABLE"
            ? colors.semYellow
            : colors.semRed;
    return (<View style={[
            styles.card,
            {
                backgroundColor: colors.card,
                borderColor: statusColor + "44",
                borderWidth: 1.5,
            },
        ]}>
      <View style={styles.statusRow}>
        <StatusBadge status={result.status} size="lg"/>
      </View>

      <View style={[styles.divider, { backgroundColor: colors.border }]}/>

      <View style={styles.mainMetric}>
        <Text style={[styles.bigLabel, { color: colors.mutedForeground }]}>
          Ganancia por km real
        </Text>
        <Text style={[styles.bigValue, { color: statusColor }]}>
          ${result.earningsPerKmReal.toFixed(2)}
          <Text style={styles.unit}>/km</Text>
        </Text>
      </View>

      <View style={styles.grid}>
        <View style={[styles.gridItem, { backgroundColor: colors.secondary + "88" }]}>
          <Text style={[styles.gridLabel, { color: colors.mutedForeground }]}>
            $/km sólo viaje
          </Text>
          <Text style={[styles.gridValue, { color: colors.foreground }]}>
            ${result.earningsPerKmTrip.toFixed(2)}
          </Text>
        </View>
        <View style={[styles.gridItem, { backgroundColor: colors.secondary + "88" }]}>
          <Text style={[styles.gridLabel, { color: colors.mutedForeground }]}>
            Ganancia total
          </Text>
          <Text style={[styles.gridValue, { color: colors.foreground }]}>
            ${result.earnings.toFixed(2)}
          </Text>
        </View>
        <View style={[styles.gridItem, { backgroundColor: colors.secondary + "88" }]}>
          <Text style={[styles.gridLabel, { color: colors.mutedForeground }]}>
            Muerto (acercamiento)
          </Text>
          <Text style={[styles.gridValue, { color: colors.foreground }]}>
            {result.pickupUnit === "m"
            ? `${result.pickupDistance}m`
            : `${result.pickupDistance.toFixed(2)}km`}
          </Text>
        </View>
        <View style={[styles.gridItem, { backgroundColor: colors.secondary + "88" }]}>
          <Text style={[styles.gridLabel, { color: colors.mutedForeground }]}>
            Distancia total
          </Text>
          <Text style={[styles.gridValue, { color: colors.foreground }]}>
            {result.totalDistance.toFixed(2)} km
          </Text>
        </View>
      </View>
    </View>);
}
const styles = StyleSheet.create({
    card: {
        borderRadius: 18,
        padding: 18,
        gap: 14,
    },
    statusRow: {
        alignItems: "center",
    },
    divider: {
        height: 1,
    },
    mainMetric: {
        alignItems: "center",
        gap: 4,
    },
    bigLabel: {
        fontFamily: "Inter_500Medium",
        fontSize: 13,
        letterSpacing: 0.3,
    },
    bigValue: {
        fontFamily: "Inter_700Bold",
        fontSize: 44,
    },
    unit: {
        fontSize: 20,
    },
    grid: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: 8,
    },
    gridItem: {
        flex: 1,
        minWidth: "45%",
        borderRadius: 10,
        padding: 10,
        gap: 3,
    },
    gridLabel: {
        fontFamily: "Inter_400Regular",
        fontSize: 11,
    },
    gridValue: {
        fontFamily: "Inter_600SemiBold",
        fontSize: 16,
    },
});
