import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useMemo, useState, useEffect } from "react";
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text, TextInput, TouchableOpacity, View, NativeModules, DeviceEventEmitter, } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useColors } from "@/hooks/useColors";
import { useTripContext } from "@/context/TripContext";
import { showTripNotification } from "@/hooks/useNotification";
const { SharedDataModule } = NativeModules;
export default function CalculatorScreen() {
    const colors = useColors();
    const insets = useSafeAreaInsets();
    const { calculateTrip, addToHistory } = useTripContext();
    const [earnings, setEarnings] = useState("");
    const [pickup, setPickup] = useState("");
    const [pickupUnit, setPickupUnit] = useState("km");
    const [tripDist, setTripDist] = useState("");
    const [saved, setSaved] = useState(false);
    const [notified, setNotified] = useState(false);
    const [modoFlotante, setModoFlotante] = useState(false);
    // Escucha de eventos nativos del Servicio de Accesibilidad
    useEffect(() => {
        // Intento 1: Eventos nativos (Es la forma correcta y eficiente en Android)
        const subscription = DeviceEventEmitter.addListener("onTripDataReceived", (data) => {
            if (data.earnings)
                setEarnings(data.earnings.toString());
            if (data.tripDist)
                setTripDist(data.tripDist.toString());
        });
        // Intento 2: Bucle de respaldo por si tu módulo nativo usa getters estáticos
        const interval = setInterval(() => {
            if (SharedDataModule && SharedDataModule.obtenerCalculoEnVivo) {
                SharedDataModule.obtenerCalculoEnVivo((gananciaNativa, distanciaNativa) => {
                    if (gananciaNativa > 0)
                        setEarnings(gananciaNativa.toString());
                    if (distanciaNativa > 0)
                        setTripDist(distanciaNativa.toString());
                });
            }
        }, 1000);
        return () => {
            subscription.remove();
            clearInterval(interval);
        };
    }, []);
    function toggleModoFlotante() {
        if (Platform.OS !== "web") {
            Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
        }
        if (modoFlotante) {
            SharedDataModule?.detenerModoFlotante();
            setModoFlotante(false);
        }
        else {
            SharedDataModule?.iniciarModoFlotante();
            setModoFlotante(true);
        }
    }
    const result = useMemo(() => {
        const e = parseFloat(earnings);
        const p = parseFloat(pickup) || 0;
        const t = parseFloat(tripDist);
        if (isNaN(e) || e <= 0 || isNaN(t) || t <= 0)
            return null;
        return calculateTrip(e, p, pickupUnit, t, "");
    }, [earnings, pickup, pickupUnit, tripDist, calculateTrip]);
    const prevResult = React.useRef(null);
    if (result !== prevResult.current) {
        prevResult.current = result;
        if (saved)
            setSaved(false);
        if (notified)
            setNotified(false);
    }
    function handleSave() {
        if (!result)
            return;
        if (Platform.OS !== "web") {
            Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
        }
        addToHistory(result);
        setSaved(true);
    }
    function handleNotify() {
        if (!result)
            return;
        showTripNotification(result);
        setNotified(true);
    }
    function handleClear() {
        setEarnings("");
        setPickup("");
        setPickupUnit("km");
        setTripDist("");
        setSaved(false);
        setNotified(false);
    }
    const statusColor = result
        ? result.status === "EXCELENTE"
            ? colors.semGreen
            : result.status === "ACEPTABLE"
                ? colors.semYellow
                : colors.semRed
        : colors.semRed;
    const statusLabel = result ? result.status : "ESPERANDO VIAJE";
    const topPad = Platform.OS === "web" ? 67 : 0;
    return (<KeyboardAvoidingView style={{ flex: 1, backgroundColor: colors.background }} behavior={Platform.OS === "ios" ? "padding" : undefined}>
      <ScrollView style={{ flex: 1 }} contentContainerStyle={[
            styles.content,
            {
                paddingTop: topPad + 20,
                paddingBottom: insets.bottom + (Platform.OS === "web" ? 34 : 110),
            },
        ]} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        {/* HEADER */}
        <View style={styles.header}>
          <View>
            <Text style={[styles.title, { color: colors.foreground }]}>Calculadora</Text>
            <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>Análisis de rentabilidad</Text>
          </View>
          <TouchableOpacity onPress={handleClear} style={[styles.clearBtn, { backgroundColor: colors.card }]} hitSlop={8}>
            <Feather name="refresh-cw" size={16} color={colors.primary}/>
          </TouchableOpacity>
        </View>

        {/* BOTÓN SUPERPOSICIÓN (ESTILO MATERIAL FLOATING ACTION) */}
        <TouchableOpacity onPress={toggleModoFlotante} style={[
            styles.materialButton,
            {
                backgroundColor: modoFlotante ? colors.semGreen : colors.primary,
                shadowColor: modoFlotante ? colors.semGreen : colors.primary,
            },
        ]}>
          <Feather name={modoFlotante ? "layers" : "layers"} size={18} color={colors.primaryForeground}/>
          <Text style={[styles.materialButtonText, { color: colors.primaryForeground }]}>
            {modoFlotante ? "SUPERPOSICIÓN EN VIVO" : "REINICIAR VENTANA FLOTANTE"}
          </Text>
        </TouchableOpacity>

        {/* CONTENEDOR DE INPUTS (TARJETA MATERIAL ELEVADA) */}
        <View style={[styles.materialCard, { backgroundColor: colors.card }]}>
          <View style={styles.field}>
            <Text style={[styles.materialLabel, { color: colors.primary }]}>Ganancia ofrecida (MXN)</Text>
            <TextInput style={[styles.materialInput, { color: colors.foreground, backgroundColor: colors.background }]} placeholder="$0.00" placeholderTextColor={colors.mutedForeground} keyboardType="decimal-pad" value={earnings} onChangeText={setEarnings}/>
          </View>

          <View style={styles.row}>
            <View style={[styles.field, { flex: 1 }]}>
              <Text style={[styles.materialLabel, { color: colors.primary }]}>Distancia al usuario</Text>
              <TextInput style={[styles.materialInput, { color: colors.foreground, backgroundColor: colors.background }]} placeholder="0.0" placeholderTextColor={colors.mutedForeground} keyboardType="decimal-pad" value={pickup} onChangeText={setPickup}/>
            </View>
            <TouchableOpacity onPress={() => setPickupUnit((u) => (u === "km" ? "m" : "km"))} style={[styles.unitBadge, { backgroundColor: colors.secondary }]}>
              <Text style={{ color: colors.foreground, fontWeight: "bold" }}>{pickupUnit}</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.field}>
            <Text style={[styles.materialLabel, { color: colors.primary }]}>Distancia del viaje completo (km)</Text>
            <TextInput style={[styles.materialInput, { color: colors.foreground, backgroundColor: colors.background }]} placeholder="0.0 km" placeholderTextColor={colors.mutedForeground} keyboardType="decimal-pad" value={tripDist} onChangeText={setTripDist}/>
          </View>
        </View>

        {/* PANEL DE RESULTADOS (DISEÑO MATERIAL CON RELIEVE Y COLOR SEMÁFORO) */}
        <View style={[styles.resultCard, { backgroundColor: colors.card, borderLeftColor: statusColor }]}>
          <View style={styles.semRow}>
            <Text style={styles.resultTitle}>ESTADO DEL VIAJE</Text>
            <View style={[styles.badgeContainer, { backgroundColor: statusColor + "15" }]}>
              <Text style={[styles.badgeText, { color: statusColor }]}>{statusLabel}</Text>
            </View>
          </View>

          <View style={styles.mainScoreContainer}>
            <Text style={styles.scoreSub}>Rendimiento por KM Real</Text>
            <Text style={[styles.scoreValue, { color: statusColor }]}>
              ${result ? result.earningsPerKmReal.toFixed(2) : "0.00"}<Text style={styles.scoreUnit}>/km</Text>
            </Text>
          </View>

          <View style={styles.statsGrid}>
            <View style={styles.statBox}>
              <Text style={styles.statLabel}>Ganancia Neta</Text>
              <Text style={[styles.statValue, { color: colors.foreground }]}>
                ${result ? result.earnings.toFixed(2) : "0.00"}
              </Text>
            </View>
            <View style={styles.statBox}>
              <Text style={styles.statLabel}>Recorrido Total</Text>
              <Text style={[styles.statValue, { color: colors.foreground }]}>
                {result ? result.totalDistance.toFixed(1) : "0.0"} km
              </Text>
            </View>
          </View>
        </View>

        {/* BOTONES DE ACCIÓN */}
        {result && (<View style={styles.actions}>
            <TouchableOpacity onPress={handleSave} disabled={saved} style={[styles.actionBtn, { backgroundColor: saved ? colors.border : colors.card }]}>
              <Feather name="download" size={16} color={saved ? colors.mutedForeground : colors.primary}/>
              <Text style={[styles.actionBtnText, { color: saved ? colors.mutedForeground : colors.foreground }]}>
                {saved ? "Guardado en Historial" : "Guardar Registro"}
              </Text>
            </TouchableOpacity>

            {Platform.OS !== "web" && (<TouchableOpacity onPress={handleNotify} disabled={notified} style={[styles.actionBtn, { backgroundColor: colors.primary }]}>
                <Feather name="bell" size={16} color={colors.primaryForeground}/>
                <Text style={[styles.actionBtnText, { color: colors.primaryForeground, fontWeight: "600" }]}>
                  {notified ? "Notificación Activa" : "Lanzar Alerta"}
                </Text>
              </TouchableOpacity>)}
          </View>)}
      </ScrollView>
    </KeyboardAvoidingView>);
}
const styles = StyleSheet.create({
    content: { paddingHorizontal: 16, gap: 20 },
    header: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
    title: { fontSize: 28, fontWeight: "bold", letterSpacing: -0.5 },
    subtitle: { fontSize: 14, marginTop: 2 },
    clearBtn: { borderRadius: 50, padding: 12, elevation: 2, shadowRadius: 2, shadowOpacity: 0.1, shadowOffset: { width: 0, height: 1 } },
    materialButton: { flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8, borderRadius: 28, paddingVertical: 15, elevation: 4, shadowOpacity: 0.25, shadowRadius: 3.84, shadowOffset: { width: 0, height: 2 } },
    materialButtonText: { fontSize: 14, fontWeight: "bold", letterSpacing: 1 },
    materialCard: { borderRadius: 16, padding: 16, gap: 14, elevation: 3, shadowOpacity: 0.1, shadowRadius: 4, shadowOffset: { width: 0, height: 2 } },
    field: { gap: 6 },
    materialLabel: { fontSize: 12, fontWeight: "700", letterSpacing: 0.3, textTransform: "uppercase" },
    materialInput: { borderRadius: 8, paddingHorizontal: 12, paddingVertical: 10, fontSize: 16, fontWeight: "500" },
    row: { flexDirection: "row", gap: 10, alignItems: "end" },
    unitBadge: { height: 45, paddingHorizontal: 16, borderRadius: 8, justifyContent: "center", alignItems: "center" },
    resultCard: { borderRadius: 16, padding: 18, borderLeftWidth: 6, elevation: 4, shadowOpacity: 0.15, shadowRadius: 5, shadowOffset: { width: 0, height: 2 } },
    semRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
    resultTitle: { fontSize: 12, fontWeight: "800", color: "#888", letterSpacing: 0.8 },
    badgeContainer: { paddingVertical: 4, paddingHorizontal: 10, borderRadius: 12 },
    badgeText: { fontSize: 11, fontWeight: "900" },
    mainScoreContainer: { alignItems: "center", marginVertical: 12 },
    scoreSub: { fontSize: 13, color: "#777", marginBottom: 2 },
    scoreValue: { fontSize: 44, fontWeight: "bold" },
    scoreUnit: { fontSize: 16, fontWeight: "normal" },
    statsGrid: { flexDirection: "row", gap: 12, marginTop: 6 },
    statBox: { flex: 1, backgroundColor: "rgba(0,0,0,0.02)", padding: 10, borderRadius: 8 },
    statLabel: { fontSize: 11, color: "#888", marginBottom: 2 },
    statValue: { fontSize: 16, fontWeight: "700" },
    actions: { flexDirection: "row", gap: 10 },
    actionBtn: { flex: 1, flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8, paddingVertical: 14, borderRadius: 12, elevation: 2 },
    actionBtnText: { fontSize: 13 }
});
