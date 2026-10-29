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

drop table "BSCL.Normativa.BD::TRI.DatosDJ1885";

/*==============================================================*/
/* Create Table:  "BSCL.Normativa.BD::TRI.DatosDJ1885"          */
/*==============================================================*/
/*
 Contiene los datos de la DatosDJ1885.
 Permite el ingreso de datos nulos, pues no en todas las ediciones del documento se informan todos los campos o existen campos condicionados entre si.
*/
create column table "BSCL.Normativa.BD::TRI.DatosDJ1885" (
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

    -- RUT del cliente.
    -- Nota: Existen RUT de 9 digitos, 100.000.000-1, no los tolera el SII,
    -- pero deben registrar operacion, aunque la validacion
    "RutCliente" nvarchar(40) null,

    -- Identificador unico de cliente del registro Base Cliente.
    "IdCliente" bigint null,

    -- Identificador unico de Portafolio del registro Base Cliente.
    "IdPortafolio" bigint null,

    -- Tipo de Documento a declarar.
    -- ACN, CFM
    "TipoInstrumento" nvarchar(40) not null,

    -- RECIBIDO / PAGADO.
    "TipoRegistro" nvarchar(1) not null,

    -- Nemotecnico del instrumento.
    "Nemotecnico" nvarchar(100) null,

    -- Fecha de Pago del Dividendo.
    "FechaPago" date null,

    -- Secuencia del evento.
    "SecuenciaEvento" bigint null,

    -- RUT del Sociedad Pagadora.
    "RutSociedad" nvarchar(40) null,

    -- Razon social de la sociedad.
    "RazonSocial" nvarchar(100) null,

    -- Tipo del Sociedad Pagadora: ABIERTA, CERRADA.
    "TipoSociedad" nvarchar(40) null,

    -- Monto pagado.
    "MontoPagado" bigint not null,

    -- Monto recibido.
    "MontoRecibido" bigint not null,

    -- Cantidad de acciones transadas.
    "CantidadAcciones31Dic" bigint not null,

    -- Dividendos.
    "DividendosAfectos" bigint not null,
    "DividendosExentos" bigint not null,
    "DividendosNoRentaNoAcogidos" bigint not null,
    "DividendosNoRentaAcogidos" bigint not null,

    -- Incremento por Impuesto Primera Categoria.
    "IncrementoImpuesto" bigint not null,

    -- Creditos.
    "CreditosAfectosConDevolucion" bigint not null,
    "CreditosAfectosSinDevolucion" bigint not null,
    "CreditosExentosConDevolucion" bigint not null,
    "CreditosExentosSinDevolucion" bigint not null,
    "CreditosImpuestosExternos" bigint not null,
    "CreditosImpuestoTasaAdicional" bigint not null,
    
    -- Numero de certificado.
    "NumeroCertificado" bigint not null,

    -- Interpretación respecto al tipo Operacion Original.
    "CantidadDerecho" bigint not null,

    "IdInstrumento" bigint not null,
    "IdEventoCapitalCliente" bigint not null,

    -- CLT, COR, CIA, BOL, CAM, CUS
    "TipoSocioNegocio" nvarchar(1) not null,
    "TipoCartera" nvarchar(3) not null,
    "IdMoneda" nvarchar(3) not null,
    "TipoEventoCapital" nvarchar(3) not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DatosDJ1885"
    add constraint "FK_Documento_DatosDJ1885" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete restrict on update restrict;

create index "IX01_DocumentoDJ1885" on "BSCL.Normativa.BD::TRI.DatosDJ1885"
    ("IdDocumento", "TipoInstrumento", "SecuenciaEvento");
    
