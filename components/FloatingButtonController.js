import React, { useEffect } from "react";
import { View, Button, StyleSheet, ToastAndroid, Platform } from "react-native";
import { initialize, showFloatingBubble, hideFloatingBubble, requestPermission, checkPermission } from "react-native-floating-bubble";
import { DeviceEventEmitter } from "react-native";
export default function FloatingButtonController() {
    useEffect(() => {
        initialize();
        const onPressSubscription = DeviceEventEmitter.addListener("floating-bubble-press", (e) => {
            ToastAndroid.show("¡Procesando datos de viaje en segundo plano!", ToastAndroid.SHORT);
        });
        const onRemoveSubscription = DeviceEventEmitter.addListener("floating-bubble-remove", (e) => {
            ToastAndroid.show("Botón flotante cerrado", ToastAndroid.SHORT);
        });
        return () => {
            onPressSubscription.remove();
            onRemoveSubscription.remove();
        };
    }, []);
    const iniciarBurbuja = async () => {
        if (Platform.OS === "android") {
            try {
                const hasPermission = await checkPermission();
                if (!hasPermission) {
                    ToastAndroid.show("Por favor, concede el permiso para mostrar sobre otras apps", ToastAndroid.LONG);
                    await requestPermission();
                    return;
                }
                await showFloatingBubble(200, 400);
                ToastAndroid.show("Botón flotante activo sobre Uber/DiDi", ToastAndroid.SHORT);
            }
            catch (error) {
                console.error("Error al iniciar botón flotante:", error);
            }
        }
    };
    const detenerBurbuja = () => {
        hideFloatingBubble()
            .then(() => ToastAndroid.show("Botón flotante ocultado", ToastAndroid.SHORT))
            .catch((err) => console.log("Error al ocultar:", err));
    };
    return (<View style={styles.container}>
      <Button title="Activar Botón Flotante" onPress={iniciarBurbuja} color="#2ecc71"/>
      <View style={{ marginVertical: 10 }}/>
      <Button title="Desactivar Botón Flotante" onPress={detenerBurbuja} color="#e74c3c"/>
    </View>);
}
const styles = StyleSheet.create({
    container: {
        padding: 20,
        justifyContent: "center",
        alignItems: "center",
    },
});
