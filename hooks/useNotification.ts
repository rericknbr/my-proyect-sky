import { CalculationResult } from "@/context/TripContext";

export async function requestNotificationPermission(): Promise<boolean> {
  return false;
}

export async function showTripNotification(
  _result: CalculationResult
): Promise<void> {}

export async function dismissTripNotification(): Promise<void> {}
