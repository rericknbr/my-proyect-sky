import * as Notifications from "expo-notifications";
Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: false,
        shouldSetBadge: false,
    }),
});
const NOTIF_ID_KEY = "viaje-rentable-live";
export async function requestNotificationPermission() {
    try {
        const { status: existing } = await Notifications.getPermissionsAsync();
        if (existing === "granted")
            return true;
        const { status } = await Notifications.requestPermissionsAsync();
        return status === "granted";
    }
    catch {
        return false;
    }
}
export async function showTripNotification(result) {
    try {
        const granted = await requestNotificationPermission();
        if (!granted)
            return;
        await Notifications.dismissAllNotificationsAsync().catch(() => { });
        const statusEmoji = result.status === "EXCELENTE"
            ? "🟢"
            : result.status === "ACEPTABLE"
                ? "🟡"
                : "🔴";
        const title = `${statusEmoji} ${result.status} — $${result.earningsPerKmReal.toFixed(2)}/km`;
        const body = `${result.platform}  ·  Ganancia: $${result.earnings.toFixed(2)}  ·  ` +
            `Total: ${result.totalDistance.toFixed(2)} km`;
        await Notifications.scheduleNotificationAsync({
            identifier: NOTIF_ID_KEY,
            content: {
                title,
                body,
                data: { result },
            },
            trigger: null,
        });
    }
    catch { }
}
export async function dismissTripNotification() {
    try {
        await Notifications.dismissAllNotificationsAsync();
    }
    catch { }
}
