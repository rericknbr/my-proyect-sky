import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useEffect, useState } from "react";
import { Alert, Platform, ScrollView, StyleSheet, Text, TextInput, TouchableOpacity, View, } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useColors } from "@/hooks/useColors";
import { useTripContext } from "@/context/TripContext";
export default function SettingsScreen() {
    const colors = useColors();
    const insets = useSafeAreaInsets();
    const { config, updateConfig } = useTripContext();
    const [greenStr, setGreenStr] = useState(config.limitGreen.toString());
    const [yellowStr, setYellowStr] = useState(config.limitYellow.toString());
    const [saved, setSaved] = useState(false);
    useEffect(() => {
        setGreenStr(config.limitGreen.toString());
        setYellowStr(config.limitYellow.toString());
    }, [config]);
    async function handleSave() {
        const green = parseFloat(greenStr);
        const yellow = parseFloat(yellowStr);
        if (isNaN(green) || isNaN(yellow) || green <= 0 || yellow <= 0) {
            Alert.alert("Valores inválidos", "Ingresa valores numéricos mayores a 0.");
            return;
        }
        if (yellow >= green) {
            Alert.alert("Valores inválidos", "El límite EXCELENTE debe ser mayor que el límite ACEPTABLE.");
            return;
        }
        if (Platform.OS !== "web") {
            Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
        }
        await updateConfig(green, yellow);
        setSaved(true);
        setTimeout(() => setSaved(false), 2000);
    }
    const topPad = Platform.OS === "web" ? 67 : 0;
    return (<ScrollView style={[{ backgroundColor: colors.background }]} contentContainerStyle={[
            styles.content,
            {
                paddingTop: topPad + 16,
                paddingBottom: insets.bottom + (Platform.OS === "web" ? 34 : 100),
            },
        ]} showsVerticalScrollIndicator={false} keyboardShouldPersistTaps="handled">
      <View>
        <Text style={[styles.title, { color: colors.foreground }]}>
          Configuración
        </Text>
        <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
          Personaliza los límites de rentabilidad
        </Text>
      </View>

      <View style={[
            styles.card,
            { backgroundColor: colors.card, borderColor: colors.border },
        ]}>
        <Text style={[styles.cardTitle, { color: colors.foreground }]}>
          Semáforo de rentabilidad
        </Text>
        <Text style={[styles.cardDesc, { color: colors.mutedForeground }]}>
          Define los umbrales de ganancia por kilómetro (incluyendo acercamiento).
        </Text>

        <View style={styles.thresholds}>
          <View style={styles.threshold}>
            <View style={[styles.thresholdIndicator, { backgroundColor: colors.semGreen }]}/>
            <View style={styles.thresholdContent}>
              <Text style={[styles.thresholdLabel, { color: colors.foreground }]}>
                EXCELENTE — ≥
              </Text>
              <View style={styles.thresholdInput}>
                <TextInput style={[
            styles.input,
            {
                backgroundColor: colors.secondary,
                borderColor: colors.semGreen + "66",
                color: colors.foreground,
            },
        ]} value={greenStr} onChangeText={(v) => {
            setGreenStr(v);
            setSaved(false);
        }} keyboardType="decimal-pad" selectTextOnFocus/>
                <Text style={[styles.unit, { color: colors.mutedForeground }]}>
                  MXN/km
                </Text>
              </View>
            </View>
          </View>

          <View style={[styles.divider, { backgroundColor: colors.border }]}/>

          <View style={styles.threshold}>
            <View style={[styles.thresholdIndicator, { backgroundColor: colors.semYellow }]}/>
            <View style={styles.thresholdContent}>
              <Text style={[styles.thresholdLabel, { color: colors.foreground }]}>
                ACEPTABLE — ≥
              </Text>
              <View style={styles.thresholdInput}>
                <TextInput style={[
            styles.input,
            {
                backgroundColor: colors.secondary,
                borderColor: colors.semYellow + "66",
                color: colors.foreground,
            },
        ]} value={yellowStr} onChangeText={(v) => {
            setYellowStr(v);
            setSaved(false);
        }} keyboardType="decimal-pad" selectTextOnFocus/>
                <Text style={[styles.unit, { color: colors.mutedForeground }]}>
                  MXN/km
                </Text>
              </View>
            </View>
          </View>

          <View style={[styles.divider, { backgroundColor: colors.border }]}/>

          <View style={styles.threshold}>
            <View style={[styles.thresholdIndicator, { backgroundColor: colors.semRed }]}/>
            <View style={styles.thresholdContent}>
              <Text style={[styles.thresholdLabel, { color: colors.foreground }]}>
                MALO — por debajo del límite amarillo
              </Text>
            </View>
          </View>
        </View>

        <TouchableOpacity onPress={handleSave} style={[
            styles.saveBtn,
            {
                backgroundColor: saved ? colors.semGreen + "22" : colors.primary,
                borderColor: saved ? colors.semGreen : "transparent",
                borderWidth: saved ? 1 : 0,
            },
        ]} activeOpacity={0.8}>
          <Feather name={saved ? "check" : "save"} size={16} color={saved ? colors.semGreen : colors.primaryForeground}/>
          <Text style={[
            styles.saveBtnText,
            {
                color: saved ? colors.semGreen : colors.primaryForeground,
            },
        ]}>
            {saved ? "Guardado" : "Guardar configuración"}
          </Text>
        </TouchableOpacity>
      </View>

      <View style={[
            styles.card,
            { backgroundColor: colors.card, borderColor: colors.border },
        ]}>
        <Text style={[styles.cardTitle, { color: colors.foreground }]}>
          Acerca de
        </Text>
        <View style={styles.infoRow}>
          <Feather name="info" size={15} color={colors.mutedForeground}/>
          <Text style={[styles.infoText, { color: colors.mutedForeground }]}>
            Viaje Rentable v1.0 — Calculadora de ganancia real por kilómetro para conductores de Uber y DiDi.
          </Text>
        </View>
      </View>
    </ScrollView>);
}
const styles = StyleSheet.create({
    content: {
        paddingHorizontal: 16,
        gap: 16,
    },
    title: {
        fontFamily: "Inter_700Bold",
        fontSize: 26,
    },
    subtitle: {
        fontFamily: "Inter_400Regular",
        fontSize: 14,
        marginTop: 2,
    },
    card: {
        borderRadius: 16,
        borderWidth: 1,
        padding: 16,
        gap: 14,
    },
    cardTitle: {
        fontFamily: "Inter_600SemiBold",
        fontSize: 17,
    },
    cardDesc: {
        fontFamily: "Inter_400Regular",
        fontSize: 13,
        lineHeight: 19,
    },
    thresholds: {
        gap: 0,
    },
    threshold: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: 12,
        paddingVertical: 12,
    },
    thresholdIndicator: {
        width: 4,
        height: 44,
        borderRadius: 2,
        marginTop: 2,
    },
    thresholdContent: {
        flex: 1,
        gap: 8,
    },
    thresholdLabel: {
        fontFamily: "Inter_500Medium",
        fontSize: 14,
    },
    thresholdInput: {
        flexDirection: "row",
        alignItems: "center",
        gap: 8,
    },
    input: {
        borderWidth: 1,
        borderRadius: 10,
        paddingHorizontal: 12,
        paddingVertical: 8,
        fontFamily: "Inter_600SemiBold",
        fontSize: 18,
        width: 90,
        textAlign: "center",
    },
    unit: {
        fontFamily: "Inter_400Regular",
        fontSize: 13,
    },
    divider: {
        height: 1,
    },
    saveBtn: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: 8,
        paddingVertical: 13,
        borderRadius: 12,
    },
    saveBtnText: {
        fontFamily: "Inter_600SemiBold",
        fontSize: 15,
    },
    infoRow: {
        flexDirection: "row",
        gap: 8,
        alignItems: "flex-start",
    },
    infoText: {
        fontFamily: "Inter_400Regular",
        fontSize: 13,
        flex: 1,
        lineHeight: 19,
    },
});
