set schema "BSCL";

-- Diccionario
-- Esquema
--     Campo
--     Clasificador
--         CampoClasificador
--     Cuadratura
--         CampoCuadratura
--     Homologacion
--     Parametro
--     Validacion
--     Documento ----------> TipoDocumento
--         ResumenCarga
--         ResumenGeneracion
--         ResumenCuadratura
--             DetalleCuadratura
--         ResumenValidacion
--             DetalleValidacion
--         DatosDJ1885
--         DatosDJ1891
--         ...

drop table "BSCL.Normativa.BD::TRI.DatosDJ1891";

-- Table: "BSCL.Normativa.BD::TRI.DatosDJ1891"
-- Contiene los datos de la DJ1891.
-- Permite el ingreso de datos nulos, pues no en todas las
-- ediciones del documento se informan todos los campos o
-- existen campos condicionados entre si.
create column table "BSCL.Normativa.BD::TRI.DatosDJ1891" (
    -- Id del registro.
    -- Por convencion, en todas las tablas de este tipo
    -- el nombre de esta columna debe ser Id.
    "ID" bigint not null,

    -- Id del Documento.
    "IdDocumento" bigint not null,

    -- VoBo del usuario sobre el registro (S/N).
    "VboUsuario" char(1) not null,

    -- Observacion ingresada por usuario que edito el registro.
    "Observaciones" nvarchar(250) not null,

    -- INICIAL / EXTERNA.
    "FuenteDatos" nvarchar(40) not null,

    -- Tipo de Documento a declarar.
    -- Ej: F: Factura / C: Nota de Crédito / D: Nota de Débito / O: Otro.
    "TipoDocumento" nvarchar(10) not null,

    -- Folio de Documento.
    "NumeroDocumento" bigint not null,

    -- Fecha del documento.
    "FechaDocumento" date null,

    -- RUT del emisor.
    "RutEmisor" nvarchar(40) null,

    -- Nemotecnico del instrumento.
    "Nemotecnico" nvarchar(100) null,

    -- RUT del cliente.
    -- Nota: Existen RUT de 9 digitos, 100.000.000-1, no los tolera el SII,
    -- pero deben registrar operacion, aunque la validacion
    "RutCliente" nvarchar(40) null,

    -- Cantidad de acciones transadas.
    "Cantidad" bigint not null,

    -- Monto.
    "Monto" bigint not null,

    -- Interpretación respecto al tipo Operacion Original.
    -- Ej 01: Acciones/02: Dólares/03: Oro/04: Otras operaciones de futuro/05: Opciones ....
    "TipoTransaccion" nvarchar(6) not null,

    -- Registros para identificación de Operacion origen, útil para cuadraturas y recarga de datos.

    -- Identificador del tipo de operación del registro Base de Comprobantes.
    -- Ej: Compra/Venta.
    "IdTipoOperacion" nvarchar(6) null,

    -- Identificador del tipo de documento.
    "IdTipoProducto" nvarchar(4) null,

    -- Relaciones otras fuentes.

    -- Identificador del Registro Base Origen.
    "IdBaseDocumento" bigint null,

    -- Identificador unico de cliente del registro Base Cliente.
    "IdCliente" bigint null,

    -- Identificador unico de Portafolio del registro Base Cliente.
    "IdPortafolio" bigint null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DatosDJ1891"
    add constraint "FK_DatosDJ1891_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete restrict on update restrict;

create index "IX01_DocumentoDJ1891" on "BSCL.Normativa.BD::TRI.DatosDJ1891"
    ("IdDocumento", "TipoDocumento", "NumeroDocumento");
