import { Feather } from "@expo/vector-icons";
import React from "react";
import { Alert, FlatList, Platform, StyleSheet, Text, TouchableOpacity, View, } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useColors } from "@/hooks/useColors";
import { useTripContext } from "@/context/TripContext";
import { TripCard } from "@/components/TripCard";
export default function HistoryScreen() {
    const colors = useColors();
    const insets = useSafeAreaInsets();
    const { history, deleteHistoryItem, clearHistory } = useTripContext();
    function handleClearAll() {
        Alert.alert("Borrar historial", "¿Seguro que quieres eliminar todos los viajes guardados?", [
            { text: "Cancelar", style: "cancel" },
            {
                text: "Borrar todo",
                style: "destructive",
                onPress: clearHistory,
            },
        ]);
    }
    const topPad = Platform.OS === "web" ? 67 : 0;
    return (<View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[
            styles.header,
            {
                paddingTop: topPad + 16,
                borderBottomColor: colors.border,
            },
        ]}>
        <View>
          <Text style={[styles.title, { color: colors.foreground }]}>
            Historial
          </Text>
          <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
            {history.length} viaje{history.length !== 1 ? "s" : ""} guardado
            {history.length !== 1 ? "s" : ""}
          </Text>
        </View>
        {history.length > 0 && (<TouchableOpacity onPress={handleClearAll} style={[styles.clearBtn, { borderColor: colors.destructive + "44" }]}>
            <Feather name="trash-2" size={15} color={colors.destructive}/>
            <Text style={[styles.clearBtnText, { color: colors.destructive }]}>
              Borrar todo
            </Text>
          </TouchableOpacity>)}
      </View>

      <FlatList data={history} keyExtractor={(item) => item.id} renderItem={({ item }) => (<TripCard trip={item} onDelete={deleteHistoryItem}/>)} contentContainerStyle={[
            styles.list,
            {
                paddingBottom: insets.bottom + (Platform.OS === "web" ? 34 : 100),
            },
        ]} showsVerticalScrollIndicator={false} scrollEnabled={!!history.length} ListEmptyComponent={<View style={styles.empty}>
            <Feather name="clock" size={40} color={colors.mutedForeground}/>
            <Text style={[styles.emptyTitle, { color: colors.foreground }]}>
              Sin viajes guardados
            </Text>
            <Text style={[styles.emptyText, { color: colors.mutedForeground }]}>
              Calcula un viaje y guárdalo para verlo aquí.
            </Text>
          </View>}/>
    </View>);
}
const styles = StyleSheet.create({
    container: { flex: 1 },
    header: {
        flexDirection: "row",
        alignItems: "flex-start",
        justifyContent: "space-between",
        paddingHorizontal: 16,
        paddingBottom: 14,
        borderBottomWidth: 1,
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
    clearBtn: {
        flexDirection: "row",
        alignItems: "center",
        gap: 5,
        paddingVertical: 7,
        paddingHorizontal: 12,
        borderRadius: 9,
        borderWidth: 1,
        marginTop: 4,
    },
    clearBtnText: {
        fontFamily: "Inter_500Medium",
        fontSize: 13,
    },
    list: {
        padding: 16,
        gap: 0,
    },
    empty: {
        alignItems: "center",
        justifyContent: "center",
        paddingTop: 80,
        gap: 12,
    },
    emptyTitle: {
        fontFamily: "Inter_600SemiBold",
        fontSize: 18,
    },
    emptyText: {
        fontFamily: "Inter_400Regular",
        fontSize: 14,
        textAlign: "center",
        paddingHorizontal: 32,
    },
});
