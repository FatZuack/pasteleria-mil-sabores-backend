package cl.duoc.pasteleria.config;

import cl.duoc.pasteleria.model.Categoria;
import cl.duoc.pasteleria.model.Producto;
import cl.duoc.pasteleria.model.Rol;
import cl.duoc.pasteleria.model.Usuario;
import cl.duoc.pasteleria.repository.CategoriaRepository;
import cl.duoc.pasteleria.repository.ProductoRepository;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatosIniciales(
            UsuarioRepository usuarioRepo,
            CategoriaRepository categoriaRepo,
            ProductoRepository productoRepo,
            PasswordEncoder encoder) {

        return args -> {
            // 1. Crear usuarios demo (uno por cada rol exigido en EP3) si no existen
            if (usuarioRepo.count() == 0) {
                usuarioRepo.save(new Usuario(
                    "Administrador Demo",
                    "admin@milsabores.cl",
                    "+56911111111",
                    encoder.encode("admin123"),
                    Rol.ADMIN
                ));
                usuarioRepo.save(new Usuario(
                    "Vendedor Tienda",
                    "vendedor@milsabores.cl",
                    "+56922222222",
                    encoder.encode("vendedor123"),
                    Rol.VENDEDOR
                ));
                usuarioRepo.save(new Usuario(
                    "Cliente Frecuente",
                    "cliente@milsabores.cl",
                    "+56933333333",
                    encoder.encode("123456"),
                    Rol.CLIENTE
                ));
            }

            // 2. Crear categorías y las 6 tortas iniciales de EP2 si la tabla está vacía
            if (categoriaRepo.count() == 0) {
                Categoria trad = categoriaRepo.save(
                    new Categoria("Tortas Tradicionales", "Recetas clásicas de la repostería chilena."));
                Categoria esp = categoriaRepo.save(
                    new Categoria("Tortas Especiales", "Combinaciones gourmet y chocolates seleccionados."));
                Categoria postres = categoriaRepo.save(
                    new Categoria("Postres", "Cheesecakes, pies y porciones individuales."));
                categoriaRepo.save(
                    new Categoria("Pastelería Fina", "Bocaditos y preparaciones artesanales para eventos."));

                productoRepo.save(new Producto(
                    "Torta Cuatro Leches",
                    "Bizcocho remojado en mezcla de cuatro leches con merengue dorado.",
                    22990, 12,
                    "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop&q=80",
                    trad
                ));
                productoRepo.save(new Producto(
                    "Torta Selva Negra",
                    "Bizcocho de chocolate relleno con crema chantilly y guindas seleccionadas.",
                    24990, 8,
                    "https://images.unsplash.com/photo-1606890737304-57a1ca8a5b62?w=600&auto=format&fit=crop&q=80",
                    esp
                ));
                productoRepo.save(new Producto(
                    "Cheesecake Frutos Rojos",
                    "Base crocante con suave crema de queso y salsa artesanal de frambuesas.",
                    19990, 15,
                    "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=600&auto=format&fit=crop&q=80",
                    postres
                ));
                productoRepo.save(new Producto(
                    "Torta Amor Hojarasca",
                    "Capas finas de hojarasca con manjar casero, crema pastelera y frambuesas.",
                    25990, 10,
                    "https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600&auto=format&fit=crop&q=80",
                    trad
                ));
                productoRepo.save(new Producto(
                    "Pie de Limón Merengado",
                    "Masa sableé rellena con crema de limón natural y merengue italiano.",
                    14990, 14,
                    "https://images.unsplash.com/photo-1519869325930-281384150729?w=600&auto=format&fit=crop&q=80",
                    postres
                ));
                productoRepo.save(new Producto(
                    "Torta Mousse de Chocolate",
                    "Mousse aireado de chocolate belga semi amargo sobre base crocante.",
                    23990, 6,
                    "https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=600&auto=format&fit=crop&q=80",
                    esp
                ));
            }
        };
    }
}