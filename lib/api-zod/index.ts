import { z } from "zod";
export * from "zod";

// Inyectamos el tipo y el validador que está pidiendo el servidor
export const HealthCheckResponse = z.any() as any;
export type HealthCheckResponse = any;

export const apiSchema = z.object({});
