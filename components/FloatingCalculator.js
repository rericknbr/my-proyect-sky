import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useMemo, useRef, useState } from "react";
import { Animated, Dimensions, KeyboardAvoidingView, PanResponder, Platform, StyleSheet, Text, TextInput, TouchableOpacity, View, } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useColors } from "@/hooks/useColors";
import { useTripContext } from "@/context/TripContext";
const FAB_SIZE = 50;
const PANEL_W = 255;
const PANEL_H = 220; // shorter now (no Calcular button)
export function FloatingCalculator() {
    const colors = useColors();
    const insets = useSafeAreaInsets();
    const { calculateTrip, addToHistory } = useTripContext();
    // Open state + ref (ref avoids stale closure inside PanResponder)
    const [open, setOpen] = useState(false);
    const openRef = useRef(false);
    function setOpenState(v) {
        openRef.current = v;
        setOpen(v);
    }
    const [earnings, setEarnings] = useState("");
    const [pickup, setPickup] = useState("");
    const [pickupUnit, setPickupUnit] = useState("km");
    const [tripDist, setTripDist] = useState("");
    const [saved, setSaved] = useState(false);
    // ── Live calculation — updates on every keystroke, no button needed ──
    const result = useMemo(() => {
        const e = parseFloat(earnings);
        const p = parseFloat(pickup) || 0;
        const t = parseFloat(tripDist);
        if (isNaN(e) || e <= 0 || isNaN(t) || t <= 0)
            return null;
        setSaved(false);
        return calculateTrip(e, p, pickupUnit, t, "");
    }, [earnings, pickup, pickupUnit, tripDist, calculateTrip]);
    const { width: SCREEN_W, height: SCREEN_H } = Dimensions.get("window");
    const tabBarH = Platform.OS === "web" ? 84 : 80;
    const safeBottom = insets.bottom + tabBarH + 14;
    const initX = SCREEN_W - FAB_SIZE - 16;
    const initY = SCREEN_H - safeBottom - FAB_SIZE;
    const committed = useRef({ x: initX, y: initY });
    const pan = useRef(new Animated.ValueXY({ x: initX, y: initY })).current;
    const panelOffX = useRef(new Animated.Value(calcOffX(initX))).current;
    const panelOffY = useRef(new Animated.Value(calcOffY(initY))).current;
    const panelTransX = useRef(Animated.add(pan.x, panelOffX)).current;
    const panelTransY = useRef(Animated.add(pan.y, panelOffY)).current;
    const panelAnim = useRef(new Animated.Value(0)).current;
    function calcOffX(fabX) {
        return fabX + FAB_SIZE / 2 >= SCREEN_W / 2
            ? -(PANEL_W + 8)
            : FAB_SIZE + 8;
    }
    function calcOffY(fabY) {
        return fabY < (insets.top || 40) + PANEL_H + 40
            ? FAB_SIZE + 8
            : -(PANEL_H + 8);
    }
    function openPanel() {
        if (Platform.OS !== "web") {
            Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
        }
        setOpenState(true);
        Animated.spring(panelAnim, {
            toValue: 1,
            useNativeDriver: true,
            tension: 200,
            friction: 14,
        }).start();
    }
    function closePanel() {
        Animated.timing(panelAnim, {
            toValue: 0,
            duration: 130,
            useNativeDriver: true,
        }).start(() => setOpenState(false));
    }
    const panResponder = useRef(PanResponder.create({
        onStartShouldSetPanResponder: () => true,
        onMoveShouldSetPanResponder: (_, g) => Math.abs(g.dx) > 4 || Math.abs(g.dy) > 4,
        onPanResponderGrant: () => {
            pan.setOffset({ x: committed.current.x, y: committed.current.y });
            pan.setValue({ x: 0, y: 0 });
        },
        onPanResponderMove: (_, g) => {
            pan.setValue({ x: g.dx, y: g.dy });
        },
        onPanResponderRelease: (_, g) => {
            pan.flattenOffset();
            const moved = Math.abs(g.dx) + Math.abs(g.dy);
            if (moved < 8) {
                Animated.spring(pan, {
                    toValue: { x: committed.current.x, y: committed.current.y },
                    useNativeDriver: false,
                    tension: 160,
                    friction: 10,
                }).start();
                if (openRef.current) {
                    closePanel();
                }
                else {
                    openPanel();
                }
                return;
            }
            const rawX = committed.current.x + g.dx;
            const rawY = committed.current.y + g.dy;
            const snapX = rawX + FAB_SIZE / 2 < SCREEN_W / 2 ? 16 : SCREEN_W - FAB_SIZE - 16;
            const minY = (insets.top || 40) + 8;
            const maxY = SCREEN_H - safeBottom - FAB_SIZE - 8;
            const snapY = Math.max(minY, Math.min(maxY, rawY));
            committed.current = { x: snapX, y: snapY };
            panelOffX.setValue(calcOffX(snapX));
            panelOffY.setValue(calcOffY(snapY));
            if (Platform.OS !== "web") {
                Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
            }
            Animated.spring(pan, {
                toValue: { x: snapX, y: snapY },
                useNativeDriver: false,
                tension: 120,
                friction: 8,
            }).start();
            if (openRef.current)
                closePanel();
        },
    })).current;
    function handleClear() {
        setEarnings("");
        setPickup("");
        setPickupUnit("km");
        setTripDist("");
        setSaved(false);
    }
    async function handleSave() {
        if (!result)
            return;
        if (Platform.OS !== "web") {
            Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
        }
        await addToHistory(result);
        setSaved(true);
    }
    const statusColor = result
        ? result.status === "EXCELENTE"
            ? colors.semGreen
            : result.status === "ACEPTABLE"
                ? colors.semYellow
                : colors.semRed
        : colors.semRed;
    const statusLabel = result ? result.status : "—";
    return (<>
      {/* ── FAB — siempre ⚡, toca para abrir/cerrar, arrastra para mover ── */}
      <Animated.View style={[
            styles.fab,
            {
                backgroundColor: colors.primary,
                transform: [{ translateX: pan.x }, { translateY: pan.y }],
            },
        ]} {...panResponder.panHandlers}>
        <View style={styles.fabInner}>
          <Feather name="zap" size={20} color={colors.primaryForeground}/>
        </View>
      </Animated.View>

      {/* ── Panel flotante — sigue al FAB vía Animated.add en tiempo real ── */}
      <Animated.View style={[
            styles.panelOuter,
            {
                transform: [
                    { translateX: panelTransX },
                    { translateY: panelTransY },
                ],
            },
        ]} pointerEvents={open ? "auto" : "none"}>
        <Animated.View style={[
            styles.panel,
            {
                backgroundColor: colors.card,
                borderColor: colors.border,
                opacity: panelAnim,
                transform: [
                    {
                        scale: panelAnim.interpolate({
                            inputRange: [0, 1],
                            outputRange: [0.85, 1],
                        }),
                    },
                ],
            },
        ]}>
          <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined}>
            {/* Encabezado */}
            <View style={styles.headerRow}>
              <Text style={[styles.panelTitle, { color: colors.foreground }]}>
                ⚡ Calculadora
              </Text>
              <TouchableOpacity onPress={handleClear} hitSlop={10}>
                <Feather name="rotate-ccw" size={13} color={colors.mutedForeground}/>
              </TouchableOpacity>
            </View>

            {/* Ganancia */}
            <TextInput style={[
            styles.input,
            {
                backgroundColor: colors.background,
                borderColor: colors.border,
                color: colors.foreground,
            },
        ]} placeholder="💰 Ganancia MXN" placeholderTextColor={colors.mutedForeground} keyboardType="decimal-pad" value={earnings} onChangeText={setEarnings} returnKeyType="next"/>

            {/* Acercamiento + unidad */}
            <View style={styles.row}>
              <TextInput style={[
            styles.input,
            styles.inputFlex,
            {
                backgroundColor: colors.background,
                borderColor: colors.border,
                color: colors.foreground,
                marginBottom: 0,
            },
        ]} placeholder="📍 Acercamiento" placeholderTextColor={colors.mutedForeground} keyboardType="decimal-pad" value={pickup} onChangeText={setPickup} returnKeyType="next"/>
              <TouchableOpacity onPress={() => setPickupUnit((u) => (u === "km" ? "m" : "km"))} style={[styles.unitBtn, { backgroundColor: colors.primary }]}>
                <Text style={[styles.unitText, { color: colors.primaryForeground }]}>
                  {pickupUnit}
                </Text>
              </TouchableOpacity>
            </View>

            {/* Distancia viaje */}
            <TextInput style={[
            styles.input,
            {
                backgroundColor: colors.background,
                borderColor: colors.border,
                color: colors.foreground,
            },
        ]} placeholder="🛣 Distancia viaje km" placeholderTextColor={colors.mutedForeground} keyboardType="decimal-pad" value={tripDist} onChangeText={setTripDist} returnKeyType="done"/>

            {/* ── Resultado en tiempo real ── */}
            <View style={[
            styles.resultBox,
            {
                backgroundColor: statusColor + "18",
                borderColor: statusColor + "55",
            },
        ]}>
              <View style={styles.resultTop}>
                <View style={[styles.semDot, { backgroundColor: statusColor }]}/>
                <Text style={[styles.statusText, { color: statusColor }]}>
                  {statusLabel}
                </Text>
                <Text style={[styles.kmValue, { color: statusColor }]}>
                  ${result ? result.earningsPerKmReal.toFixed(2) : "0.00"}/km
                </Text>
              </View>
              {result && (<View style={styles.resultBottom}>
                  <Text style={[styles.detailText, { color: colors.mutedForeground }]}>
                    Viaje {result.tripDistance.toFixed(1)} km · Total {result.totalDistance.toFixed(1)} km
                  </Text>
                  <TouchableOpacity onPress={handleSave} disabled={saved} style={[
                styles.saveBtn,
                {
                    backgroundColor: saved
                        ? colors.semGreen + "22"
                        : colors.primary + "33",
                },
            ]}>
                    <Feather name={saved ? "check" : "bookmark"} size={12} color={saved ? colors.semGreen : colors.primary}/>
                  </TouchableOpacity>
                </View>)}
            </View>
          </KeyboardAvoidingView>
        </Animated.View>
      </Animated.View>
    </>);
}
const styles = StyleSheet.create({
    fab: {
        position: "absolute",
        top: 0,
        left: 0,
        width: FAB_SIZE,
        height: FAB_SIZE,
        borderRadius: FAB_SIZE / 2,
        zIndex: 1000,
        elevation: 10,
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 3 },
        shadowOpacity: 0.28,
        shadowRadius: 5,
    },
    fabInner: {
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
        borderRadius: FAB_SIZE / 2,
    },
    panelOuter: {
        position: "absolute",
        top: 0,
        left: 0,
        width: PANEL_W,
        zIndex: 999,
    },
    panel: {
        borderRadius: 16,
        borderWidth: 1,
        padding: 12,
        elevation: 9,
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 5 },
        shadowOpacity: 0.22,
        shadowRadius: 12,
    },
    headerRow: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        marginBottom: 10,
    },
    panelTitle: {
        fontFamily: "Inter_700Bold",
        fontSize: 14,
    },
    input: {
        borderWidth: 1,
        borderRadius: 9,
        paddingHorizontal: 9,
        paddingVertical: 9,
        fontFamily: "Inter_400Regular",
        fontSize: 13,
        marginBottom: 7,
    },
    inputFlex: { flex: 1 },
    row: {
        flexDirection: "row",
        gap: 6,
        alignItems: "center",
        marginBottom: 7,
    },
    unitBtn: {
        paddingHorizontal: 9,
        paddingVertical: 9,
        borderRadius: 9,
        minWidth: 38,
        alignItems: "center",
    },
    unitText: {
        fontFamily: "Inter_700Bold",
        fontSize: 12,
    },
    resultBox: {
        borderRadius: 10,
        borderWidth: 1,
        paddingVertical: 9,
        paddingHorizontal: 11,
        gap: 5,
    },
    resultTop: {
        flexDirection: "row",
        alignItems: "center",
        gap: 7,
    },
    semDot: {
        width: 9,
        height: 9,
        borderRadius: 5,
    },
    statusText: {
        fontFamily: "Inter_700Bold",
        fontSize: 12,
        flex: 1,
    },
    kmValue: {
        fontFamily: "Inter_700Bold",
        fontSize: 17,
    },
    resultBottom: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
    },
    detailText: {
        fontFamily: "Inter_400Regular",
        fontSize: 10,
        flex: 1,
    },
    saveBtn: {
        width: 26,
        height: 26,
        borderRadius: 13,
        alignItems: "center",
        justifyContent: "center",
    },
});
