package cl.duoc.pasteleria.model;

/**
 * Los 3 roles exigidos por la pauta de EP3:
 * - ADMIN: acceso total al sistema.
 * - VENDEDOR: solo lectura de productos (lista/detalle) y boletas/órdenes (lista/detalle).
 * - CLIENTE: solo acceso a la tienda y sus compras.
 */
public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE
}