import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React from "react";
import { StyleSheet, Text, TouchableOpacity, View, Platform, } from "react-native";
import { useColors } from "@/hooks/useColors";
import { StatusBadge } from "./StatusBadge";
function formatDate(timestamp) {
    const d = new Date(timestamp);
    const day = d.getDate().toString().padStart(2, "0");
    const month = (d.getMonth() + 1).toString().padStart(2, "0");
    const hours = d.getHours().toString().padStart(2, "0");
    const mins = d.getMinutes().toString().padStart(2, "0");
    return `${day}/${month} ${hours}:${mins}`;
}
export function TripCard({ trip, onDelete }) {
    const colors = useColors();
    function handleDelete() {
        if (Platform.OS !== "web") {
            Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
        }
        onDelete(trip.id);
    }
    return (<View style={[
            styles.card,
            {
                backgroundColor: colors.card,
                borderColor: colors.border,
            },
        ]}>
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <Text style={[styles.platform, { color: colors.foreground }]}>
            {trip.platform}
          </Text>
          <Text style={[styles.date, { color: colors.mutedForeground }]}>
            {formatDate(trip.timestamp)}
          </Text>
        </View>
        <View style={styles.headerRight}>
          <StatusBadge status={trip.status} size="sm"/>
          <TouchableOpacity onPress={handleDelete} style={styles.deleteBtn} hitSlop={10}>
            <Feather name="trash-2" size={16} color={colors.destructive}/>
          </TouchableOpacity>
        </View>
      </View>

      <View style={[styles.divider, { backgroundColor: colors.border }]}/>

      <View style={styles.metrics}>
        <View style={styles.metric}>
          <Text style={[styles.metricLabel, { color: colors.mutedForeground }]}>
            Ganancia
          </Text>
          <Text style={[styles.metricValue, { color: colors.foreground }]}>
            ${trip.earnings.toFixed(2)}
          </Text>
        </View>
        <View style={styles.metric}>
          <Text style={[styles.metricLabel, { color: colors.mutedForeground }]}>
            Distancia total
          </Text>
          <Text style={[styles.metricValue, { color: colors.foreground }]}>
            {trip.totalDistance.toFixed(2)} km
          </Text>
        </View>
        <View style={styles.metric}>
          <Text style={[styles.metricLabel, { color: colors.mutedForeground }]}>
            $/km real
          </Text>
          <Text style={[styles.metricValue, { color: colors.primary }]}>
            ${trip.earningsPerKmReal.toFixed(2)}
          </Text>
        </View>
        <View style={styles.metric}>
          <Text style={[styles.metricLabel, { color: colors.mutedForeground }]}>
            $/km viaje
          </Text>
          <Text style={[styles.metricValue, { color: colors.foreground }]}>
            ${trip.earningsPerKmTrip.toFixed(2)}
          </Text>
        </View>
      </View>
    </View>);
}
const styles = StyleSheet.create({
    card: {
        borderRadius: 14,
        borderWidth: 1,
        padding: 14,
        marginBottom: 10,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        marginBottom: 10,
    },
    headerLeft: { gap: 2 },
    headerRight: {
        flexDirection: "row",
        alignItems: "center",
        gap: 10,
    },
    platform: {
        fontFamily: "Inter_600SemiBold",
        fontSize: 15,
    },
    date: {
        fontFamily: "Inter_400Regular",
        fontSize: 12,
    },
    deleteBtn: {
        padding: 2,
    },
    divider: {
        height: 1,
        marginBottom: 10,
    },
    metrics: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: 8,
    },
    metric: {
        flex: 1,
        minWidth: "40%",
        gap: 2,
    },
    metricLabel: {
        fontFamily: "Inter_400Regular",
        fontSize: 11,
    },
    metricValue: {
        fontFamily: "Inter_600SemiBold",
        fontSize: 15,
    },
});
