import React from "react";
import { StyleSheet, Text, View } from "react-native";
import { useColors } from "@/hooks/useColors";
export function StatusBadge({ status, size = "md" }) {
    const colors = useColors();
    const statusColor = status === "EXCELENTE"
        ? colors.semGreen
        : status === "ACEPTABLE"
            ? colors.semYellow
            : colors.semRed;
    const label = status === "EXCELENTE"
        ? "EXCELENTE"
        : status === "ACEPTABLE"
            ? "ACEPTABLE"
            : "MALO";
    const fontSize = size === "sm" ? 10 : size === "lg" ? 16 : 12;
    const paddingV = size === "sm" ? 3 : size === "lg" ? 8 : 4;
    const paddingH = size === "sm" ? 8 : size === "lg" ? 16 : 10;
    return (<View style={[
            styles.badge,
            {
                backgroundColor: statusColor + "22",
                borderColor: statusColor,
                paddingVertical: paddingV,
                paddingHorizontal: paddingH,
            },
        ]}>
      <View style={[styles.dot, { backgroundColor: statusColor }]}/>
      <Text style={[
            styles.label,
            { color: statusColor, fontSize },
        ]}>
        {label}
      </Text>
    </View>);
}
const styles = StyleSheet.create({
    badge: {
        flexDirection: "row",
        alignItems: "center",
        borderRadius: 999,
        borderWidth: 1,
        gap: 5,
    },
    dot: {
        width: 6,
        height: 6,
        borderRadius: 3,
    },
    label: {
        fontFamily: "Inter_700Bold",
        letterSpacing: 0.5,
    },
});
