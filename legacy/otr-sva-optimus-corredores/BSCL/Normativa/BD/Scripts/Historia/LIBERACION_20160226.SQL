-- ########################################################################################
--
--  ==========================================================
--  ESTE ARCHIVO CONTIENE LOS SCRIPTS HISTORICOS DE LIBERACION
--  ==========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

set schema "BSCL";
--
--
-- LIBERACION SEMANA: 26-02-2016
--

drop table "BSCL.Normativa.BD::TRI.DatosDJ1891";
drop table "BSCL.Normativa.BD::DOC.Documento";
drop table "BSCL.Normativa.BD::DOC.TipoDocumento";
drop table "BSCL.Normativa.BD::DOC.Validacion";
drop table "BSCL.Normativa.BD::DOC.Homologacion";
drop table "BSCL.Normativa.BD::DOC.Diccionario";
drop table "BSCL.Normativa.BD::DOC.Campo";
drop table "BSCL.Normativa.BD::DOC.Esquema";

/*==============================================================*/
/* Create Tables:                                               */
/*==============================================================*/

-- Table: "BSCL.Normativa.BD::DOC.Esquema"
-- Tabla que contiene datos de especificacion base de todos los
-- tipos de documentos.
-- Representa la raiz en la jerarquia.
create column table "BSCL.Normativa.BD::DOC.Esquema" (
    -- Nombre del esquema (Ej. DJ1891).
    "Esquema" nvarchar(10) not null,

    -- Version del esquema (Ej. 2015).
    "Version" nvarchar(10) not null,

    -- Periodicidad (Ej. "A"nual , "M"ensual, "T"rimestral, "D"iaria)
    "Periodicidad" char(1) not null,

    -- Texto descriptivo del objetivo de la declaracion.
    "Objetivo" nvarchar(100) not null,

    -- Fecha desde la cual se exige presentacion
    "PeriodoInicio" date not null,

    -- Identificador de la entidad reguladora (Ej. SII)
    "IdEntidadReguladora" nvarchar(40) not null,

    -- Fecha limite para presentacion en la entidad reguladora.
    "FechaPresentacionLimite" date not null,

    primary key ("Esquema", "Version")
);

-- Table: "BSCL.Normativa.BD::DOC.Campo"
-- Especificacion de los campos del detalle de un documento,
-- en funcion de su representacion y formatos para el tratamiento
-- generico de los mismos en los programas correspondientes.
create column table "BSCL.Normativa.BD::DOC.Campo" (
    -- ID del registro.
    "IdCampo" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Tipo de diccionario relacionado.
    "TipoDiccionario" nvarchar(40) not null default '',

    -- Orden de presentacion del campo.
    "Orden" smallint not null,

    -- Nombre del campo (debe ser unico para esquema/version).
    "Nombre" nvarchar(40) not null,

    -- Texto descriptivo del campo para ser utilizado como titulo.
    -- Debiera ser unico para esquema/version.
    "Titulo" nvarchar(100) not null,

    -- Tipo de dato, segun los manejados por la aplicacion.
    "TipoDato" nvarchar(100) not null,

    -- Largo del campo.
    "Largo" smallint not null,

    -- Valor por defecto del campo.
    "ValorDefecto" nvarchar(100) not null,

    -- Indicador de si el campo es obligatorio (S/N).
    "Obligatorio" char(1) not null,

    -- Indicador de si el campo es editable (S/N).
    "Editable" char(1) not null,

    -- Indicador de si el campo es editable en modo masivo (S/N).
    "ModoMasivo" char(1) not null,

    -- Indicador de si el campo es exportable (S/N).
    "Exportable" char(1) not null,

    -- Indicador de si el campo es oculto (S/N).
    "Oculto" char(1) not null,

    -- Indicador para fijar la columna del campo en la grilla (S/N).
    "ColumnaFija" char(1) not null,

    -- Indicador de si la columna es clave (S/N).
    "Clave" char(1) not null,

    -- Indicador de si la columna es comun (S/N).
    "Comun" char(1) not null,

    -- Posición de disposicion del campo en archivo de carga.
    "OrdenCargaExterna" smallint not null,

    -- Largo del dato en carga externa.
    "LargoCargaExterna" smallint not null,

    primary key ("IdCampo")
);

alter table "BSCL.Normativa.BD::DOC.Campo"
   add constraint "FK_Esquema_Campo" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Diccionario"
-- Definicion de codigos utilizados en la edicion y presentacion del documento.
-- Sirve para llenar combos.
-- La nomenclatura o codigo es la impuesta por la entidad reguladora.
create column table "BSCL.Normativa.BD::DOC.Diccionario" (
    -- ID del registro.
    "IdDiccionario" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Tipo de diccionario (actua como clasificador).
    -- Ej. CodigoConcepto, Clasific, CodMoneda
    "Tipo" nvarchar(40) not null,

    -- Orden de presentacion en la vista.
    "Orden" smallint not null,

    -- Codigo del par (Codigo, Descripcion).
    "Codigo" nvarchar(40) not null,

    -- Descripcion del par (Codigo, Descripcion).
    "Descripcion" nvarchar(100) not null,

    -- Indicador de si el registro esta activo (S/N).
    "Activo" char(1) not null,

    -- Objetivo ...
    "Objetivo" nvarchar(100) not null,

    -- Producto del Sistema Operacional al cual aplica.
    "Producto" nvarchar(100) not null,

    primary key ("IdDiccionario")
);

alter table "BSCL.Normativa.BD::DOC.Diccionario"
   add constraint "FK_Esquema_Diccionario" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Homologacion"
-- Contiene homologación de tipos.
-- Cuando se cargan datos desde un origen operacional se debe aplicar
-- conversiones a los tipos de datos para dejar en formato segun de
-- acuerdo a los codigos definidos por la entidad reguladora.
create column table "BSCL.Normativa.BD::DOC.Homologacion" (
    -- ID del registro.
    "IdHomologacion" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Nombre del campo a homologar.
    "Tipo" nvarchar(40) not null,

    -- Descripcion del campo a homologar.
    "Descripcion" nvarchar(100) not null,

    -- Valor en el sistema de origen.
    "ValorOrigen" nvarchar(100) not null,

    -- Valor en el sistema de destino.
    "ValorDestino" nvarchar(100) not null,

    primary key ("IdHomologacion")
);

alter table "BSCL.Normativa.BD::DOC.Homologacion"
   add constraint "FK_Esquema_Homologacion" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Validacion"
-- Definición de las validaciones aplicadas a los documentos.
create column table "BSCL.Normativa.BD::DOC.Validacion" (
    -- ID del registro.
    "IdValidacion" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,
    -- Tipo de validacion (Ej: VALIDACION, CUADRATURA, NEGOCIO).
    "TipoValidacion" nvarchar(40) not null,

    -- Codigo de identificacion de la validacion (Ej: H1520).
    "CodigoValidacion" nvarchar(40) not null,

    -- Descripcion de la validacion.
    -- Ej: Si Tipo de Fondo es igual a F, entonces Tipo de Operacion debe ser igual a 1.
    "Descripcion" nvarchar(250) not null,

    -- Severidad de la validacion.
    -- "C" Correcta / "A" advertencia / "B" bloqueo.
    "Severidad" char(1) not null,

    -- Expresion de la regla de validacion.
    -- Ej. Si (C2) = F -> (C5) = 1
    "Regla" nvarchar(250) not null,

    primary key ("IdValidacion")
);

alter table "BSCL.Normativa.BD::DOC.Validacion"
   add constraint "FK_Esquema_Validacion" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.TipoDocumento"
-- Definición de Tipo de Documento,
create column table "BSCL.Normativa.BD::DOC.TipoDocumento" (
    -- Nombre del tipo de documento.
    "IdTipoDocumento" nvarchar(10) not null,

    -- Descripcion del tipo de documento.
    -- Ejemplos:
    --     + Certificados por Cliente (Requiere Firma)
    --     + Declaraciones por Corredor (Confirmacion)
    --     + Ciculares por Corredor (Confirmacion)
    --     + Circulares por Cliente (Firma)
    "Descripcion" nvarchar(250) not null,

    primary key ("IdTipoDocumento")
);

-- Table: "BSCL"."BSCL.Normativa.BD::DOC.Documento"
-- Contiene la edicion del documento.
-- Cada periodo a informar debe inscribir un registro aca.
create column table "BSCL.Normativa.BD::DOC.Documento" (
    -- ID del registro.
    "IdDocumento" bigint not null,

    -- Tipo de documento.
    "IdTipoDocumento" nvarchar(10) not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- ID de la empresa.
    "IdEmpresa" bigint not null,

    -- Fecha limite de presentacion del documento.
    -- Que pasa cuando es mensual, se van agregando ediciones mensuales?
    "FechaLimite" date not null,

    -- Estado del documento (debe ser consecuente con Estados del Flujo).
    "IdEstadoDocumento" nvarchar(40) not null,

    -- Fecha de creacion del documento.
    "FechaCreacion" seconddate not null,

    -- Fecha de presentacion del documento.
    "FechaPresentacion" seconddate null,

    -- Cantidad de registros cargados.
    "CantidadRegistrosCargados" bigint not null,

    -- SISTEMA / EXTERNA.
    "FuenteDatos" nvarchar(40) not null,

    -- Carga Inicial, Interfaz Externa.
    "ProcesoFuente" nvarchar(40) not null,

    -- Cliente Titular que presenta el documento (Corredora, Cliente)
    "IdTitularDocumento" bigint not null,

    -- Identificador Tributario o Documento Legal que identifica al cliente (RUT-TAXID)
    "DocumentoIdentidadTitular" nvarchar(40) not null,

    -- Representante Legal del Titular
    "IdRepresentanteLegal" bigint not null,

    -- Identificador Tributario o Documento Legal que identifica al Representante Legal
    "DocumentoIdentidadRepLegal" nvarchar(40) not null,

    "DireccionFisicaTitular" nvarchar(100) not null,

    "DireccionElectronicaTitular" nvarchar(100) not null,

    "TelefonoTitular" nvarchar(100) not null,

    -- Folio de Documento utilizado en las interfaces a enviar al regulador.
    -- Ej: I9153103
    "FolioPresentacionDocumento" nvarchar(100) not null,

    primary key ("IdDocumento")
);

alter table "BSCL.Normativa.BD::DOC.Documento"
   add constraint "FK_TipoDocumento_Documento" foreign key ("IdTipoDocumento")
      references "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento")
      on delete restrict on update restrict;

alter table "BSCL.Normativa.BD::DOC.Documento"
   add constraint "FK_Esquema_Documento" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::TRI.DatosDJ1891"
-- Contiene los datos de la DJ1891.
-- Permite el ingreso de datos nulos, pues no en todas las
-- ediciones del documento se informan todos los campos o
-- existen campos condicionados entre si.
create column table "BSCL.Normativa.BD::TRI.DatosDJ1891" (
    -- ID del registro.
    -- Por convencion, en todas las tablas de este tipo
    -- el nombre de esta columna debe ser ID.
    "ID" bigint not null,

    -- ID del Documento.
    "IdDocumento" bigint not null,

    -- VoBo del usuario sobre el registro (S/N).
    "VboUsuario" char(1) not null,

    -- Observacion ingresada por usuario que edito el registro.
    "Observaciones" nvarchar(250) not null,

    -- SISTEMA / EXTERNA.
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
   "Monto" decimal(15,0) not null,

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
   add constraint "FK_Documento_DatosDJ1891" foreign key ("IdDocumento")
      references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
      on delete restrict on update restrict;

/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"
select
    'DECLACORR' as "IdTipoDocumento",
    'Declaracion Jurada por Corredora' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"
select
    'CERTCLI' as "IdTipoDocumento",
    'Certificado Tributario Cliente' as "Descripcion"
from dummy;

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '1996-02-15' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2015-03-30' as "FechaPresentacionLimite"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '1996-02-15' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2016-03-30' as "FechaPresentacionLimite"
from dummy;

/*==============================================================*/
/* DICCIONARIOS                                                 */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  1, 'DJ1891', '2015', 'TipoProducto', 'INAX',         1, 'Acciones extranjeras en BCS'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  2, 'DJ1891', '2015', 'TipoProducto', 'INAC',         2, 'Intermediacion acciones'      , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  3, 'DJ1891', '2015', 'TipoProducto', 'INCI',         3, 'Intermediacion CFI'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  4, 'DJ1891', '2015', 'TipoProducto', 'INCM',         4, 'Intermediacion CFM'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  5, 'DJ1891', '2015', 'TipoProducto', 'INCX',         5, 'Intermedacion CFI Extranjeros', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  6, 'DJ1891', '2015', 'TipoProducto', 'INEN',         6, 'Intermediacion ETF nacional'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  7, 'DJ1891', '2015', 'TipoProducto', 'INEX',         7, 'Intermediacion ETF extranjero', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  8, 'DJ1891', '2015', 'TipoProducto', 'INFU',         8, 'Intermediacion futuros'       , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  9, 'DJ1891', '2015', 'TipoProducto', 'INFM',         9, 'Intermediacion fondos mutuos' , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 10, 'DJ1891', '2015', 'TipoProducto', 'INIF',        10, 'Intermediacion IIF'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 11, 'DJ1891', '2015', 'TipoProducto', 'INRF',        11, 'Intermediacion renta fija'    , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 12, 'DJ1891', '2015', 'TipoProducto', 'INSI',        12, 'Intermediación simultáneas'   , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 13, 'DJ1891', '2015', 'TipoProducto', 'MEME',        13, 'Mesa moneda extranjera (Spot)', 'S', '', 'MonedaExtranjera' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 14, 'DJ1891', '2015', 'TipoProducto', 'MERF',        14, 'Mesa pactos'                  , 'S', '', 'MesaPactos'       from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 15, 'DJ1891', '2015', 'TipoProducto', 'INMO',        15, 'Intermediación Monetarios'    , 'S', '', 'TipoProducto'     from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 16, 'DJ1891', '2015', 'TipoProducto', 'INST',        16, 'Intermediación Stocks'        , 'S', '', 'TipoProducto'     from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 17, 'DJ1891', '2015', 'TipoTransac',  '00',           1, 'Comisiones                                                                                         ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 18, 'DJ1891', '2015', 'TipoTransac',  '01',           2, 'Acciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 19, 'DJ1891', '2015', 'TipoTransac',  '02',           3, 'Dólares                                                                                            ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 20, 'DJ1891', '2015', 'TipoTransac',  '03',           4, 'Oro                                                                                                ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 21, 'DJ1891', '2015', 'TipoTransac',  '04',           5, 'Otras Operaciones de Futuro                                                                        ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 22, 'DJ1891', '2015', 'TipoTransac',  '05',           6, 'Opciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 23, 'DJ1891', '2015', 'TipoTransac',  '06',           7, 'Documentos de Renta Fija                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 24, 'DJ1891', '2015', 'TipoTransac',  '07',           8, 'Títulos Emitidos por Bolsas de Productos Agropecuarios sobre Certificados de Depósitos de Productos', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 25, 'DJ1891', '2015', 'TipoTransac',  '08',           9, 'Cuotas de Fondos de Inversión                                                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 26, 'DJ1891', '2015', 'TipoTransac',  '09',          10, 'Otros Instrumentos de Inversión                                                                    ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 27, 'DJ1891', '2015', 'TipoTransac',  '10',          11, 'Aporte y Adquisición de Valores (ETF - Exchange Traded Funds)                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 28, 'DJ1891', '2015', 'TipoTransac',  '99',          12, 'Otros que no sean instrumentos de inversión                                                        ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 29, 'DJ1891', '2015', 'TipoDocu',     'C',            1, 'Nota de Crédito',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 30, 'DJ1891', '2015', 'TipoDocu',     'D',            2, 'Nota de Débito ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 31, 'DJ1891', '2015', 'TipoDocu',     'F',            3, 'Factura        ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 32, 'DJ1891', '2015', 'TipoDocu',     'O',            4, 'Otro           ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 33, 'DJ1891', '2015', 'Origen',       'CARGAEXT',     1, 'Carga Masiva Externa ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 34, 'DJ1891', '2015', 'Origen',       'INGMANUAL',    2, 'Ingreso Manual       ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 35, 'DJ1891', '2015', 'Origen',       'OPERACIONAL',  3, 'Sebra CB             ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 36, 'DJ1891', '2015', 'TipoOper',     'CO',           1, 'Compra           ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 37, 'DJ1891', '2015', 'TipoOper',     'CP',           2, 'Compra con Pacto ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 38, 'DJ1891', '2015', 'TipoOper',     'CT',           3, 'Compra Término   ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 39, 'DJ1891', '2015', 'TipoOper',     'VE',           4, 'Venta            ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 40, 'DJ1891', '2015', 'TipoOper',     'VP',           5, 'Venta con Pacto  ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 41, 'DJ1891', '2015', 'TipoOper',     'VT',           6, 'Venta Término    ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 42, 'DJ1891', '2015', 'VistoBueno',   'N',            1, 'No Revisados por Usuario ', 'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Esquema", "Version", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 43, 'DJ1891', '2015', 'VistoBueno',   'S',            2, 'Revisados por Usuario    ', 'S', '', ''            from dummy;

/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/

--
-- DJ1891-2015
--
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    1 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    1 as "Orden",
    'ID' as "Nombre",
    'ID' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'S' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    2 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    2 as "Orden",
    'IdDocumento' as "Nombre",
    'ID Docto' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'S' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    3 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    3 as "Orden",
    'VboUsuario' as "Nombre",
    'VoBo' as "Titulo",
    'BOOL' as "TipoDato",
    1 as "Largo",
    'N' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    4 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    4 as "Orden",
    'Observaciones' as "Nombre",
    'Observaciones' as "Titulo",
    'TEXT' as "TipoDato",
    250 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    5 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    5 as "Orden",
    'FuenteDatos' as "Nombre",
    'Fuente' as "Titulo",
    'TEXT' as "TipoDato",
    0 as "Largo",
    'SISTEMA' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    6 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'TipoDocu' as "TipoDiccionario",
    6 as "Orden",
    'TipoDocumento' as "Nombre",
    'Tipo Documento' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'S' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    7 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    7 as "Orden",
    'NumeroDocumento' as "Nombre",
    'Número Documento' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'S' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    8 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    8 as "Orden",
    'FechaDocumento' as "Nombre",
    'Fecha Documento' as "Titulo",
    'DATE' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    9 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    9 as "Orden",
    'RutEmisor' as "Nombre",
    'RUT Emisor' as "Titulo",
    'RUT' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    10 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    10 as "Orden",
    'Nemotecnico' as "Nombre",
    'Nemotécnico' as "Titulo",
    'NEMO' as "TipoDato",
    100 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    11 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    11 as "Orden",
    'RutCliente' as "Nombre",
    'RUT Cliente' as "Titulo",
    'RUT_CLIENTE' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'S' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    12 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    12 as "Orden",
    'Cantidad' as "Nombre",
    'Cantidad' as "Titulo",
    'QTY' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    13 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    13 as "Orden",
    'Monto' as "Nombre",
    'Monto' as "Titulo",
    'AMOUNT' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    14 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'TipoTransac' as "TipoDiccionario",
    14 as "Orden",
    'TipoTransaccion' as "Nombre",
    'Tipo Transacción' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    15 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'TipoOper' as "TipoDiccionario",
    15 as "Orden",
    'IdTipoOperacion' as "Nombre",
    'Tipo Operación' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    16 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'TipoProducto' as "TipoDiccionario",
    16 as "Orden",
    'IdTipoProducto' as "Nombre",
    'Tipo Producto' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    17 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    17 as "Orden",
    'IdBaseDocumento' as "Nombre",
    'ID Base' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    18 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    18 as "Orden",
    'IdCliente' as "Nombre",
    'ID Cliente' as "Titulo",
    'ID_CLIENTE' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    19 as "IdCampo",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    '' as "TipoDiccionario",
    19 as "Orden",
    'IdPortafolio' as "Nombre",
    'ID Portafolio' as "Titulo",
    'ID_PORTAFOLIO' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;

--
-- DJ1891-2016
--
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    21 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    1 as "Orden",
    'ID' as "Nombre",
    'ID' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'S' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    22 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    2 as "Orden",
    'IdDocumento' as "Nombre",
    'ID Docto' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'S' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    23 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    3 as "Orden",
    'VboUsuario' as "Nombre",
    'VoBo' as "Titulo",
    'BOOL' as "TipoDato",
    1 as "Largo",
    'N' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    24 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    4 as "Orden",
    'Observaciones' as "Nombre",
    'Observaciones' as "Titulo",
    'TEXT' as "TipoDato",
    250 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    25 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    5 as "Orden",
    'FuenteDatos' as "Nombre",
    'Fuente' as "Titulo",
    'TEXT' as "TipoDato",
    40 as "Largo",
    'SISTEMA' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'S' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    26 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'TipoDocu' as "TipoDiccionario",
    6 as "Orden",
    'TipoDocumento' as "Nombre",
    'Tipo Documento' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'S' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    27 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    7 as "Orden",
    'NumeroDocumento' as "Nombre",
    'Número Documento' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'S' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    28 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    8 as "Orden",
    'FechaDocumento' as "Nombre",
    'Fecha Documento' as "Titulo",
    'DATE' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    29 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    9 as "Orden",
    'RutEmisor' as "Nombre",
    'RUT Emisor' as "Titulo",
    'RUT' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    30 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    10 as "Orden",
    'Nemotecnico' as "Nombre",
    'Nemotécnico' as "Titulo",
    'NEMO' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    31 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    11 as "Orden",
    'RutCliente' as "Nombre",
    'RUT Cliente' as "Titulo",
    'RUT_CLIENTE' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'S' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    32 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    12 as "Orden",
    'Cantidad' as "Nombre",
    'Cantidad' as "Titulo",
    'QTY' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    33 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    13 as "Orden",
    'Monto' as "Nombre",
    'Monto' as "Titulo",
    'AMOUNT' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    34 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'TipoTransac' as "TipoDiccionario",
    14 as "Orden",
    'TipoTransaccion' as "Nombre",
    'Tipo Transacción' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    35 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'TipoOper' as "TipoDiccionario",
    15 as "Orden",
    'IdTipoOperacion' as "Nombre",
    'Tipo Operación' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    36 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'TipoProducto' as "TipoDiccionario",
    16 as "Orden",
    'IdTipoProducto' as "Nombre",
    'Tipo Producto' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    37 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    17 as "Orden",
    'IdBaseDocumento' as "Nombre",
    'ID Base' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    38 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    18 as "Orden",
    'IdCliente' as "Nombre",
    'ID Cliente' as "Titulo",
    'ID_CLIENTE' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    39 as "IdCampo",
    'DJ1891' as "Esquema",
    '2016' as "Version",
    '' as "TipoDiccionario",
    19 as "Orden",
    'IdPortafolio' as "Nombre",
    'ID Portafolio' as "Titulo",
    'ID_PORTAFOLIO' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "Oculto",
    'N' as "ColumnaFija",
    'N' as "Clave",
    'N' as "Comun",
    0 as "OrdenCargaExterna",
    0 as "LargoCargaExterna"
from dummy;

/*==============================================================*/
/* DOCUMENTOS                                                   */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Documento"
select
    1 as "IdDocumento",
    'DECLACORR' as "IdTipoDocumento",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    1 as "IdEmpresa",
    '2016-03-31' as "FechaLimite",
    'ACT' as "IdEstadoDocumento",
    '2016-01-01' as "FechaCreacion",
    '2016-03-30' as "FechaPresentacion",
    11 as "CantidadRegistrosCargados",
    'SISTEMA' as "FuenteDatos",
    'CargaInicial' as "ProcesoFuente",
    1 as "IdTitularDocumento",
    '90947000-1' as "DocumentoIdentidadTitular",
    2 as "IdRepresentanteLegal",
    '14448678-1' as "DocumentoIdentidadRepLegal",
    'EL Golf 47' as "DireccionFisicaTitular",
    'contabilidad@imtrust.cl' as "DireccionElectronicaTitular",
    '56 2 25689854' as "TelefonoTitular",
    'I9153103' as "FolioPresentacionDocumento"
from dummy;

/*==============================================================*/
/* DETALLES DJ1891                                              */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1891"
select
    row_number() over (order by "IdComprobanteDetalle") as ID,
    1 as "IdDocumento",
    'N' as "VboUsuario",
    '' as "Observaciones",
    'SISTEMA' as "FuenteDatos",
    'F' as "TipoDocumento",
    comp."FolioComprobante" as "NumeroDocumento",
    comp."FechaEmision" as "FechaDocumento",
    ifnull(soc."RUT",'') as "RutEmisor",
    det."NemotecnicoInstrumento" as "Nemotecnico",
    doc."DocumentoIdentidad" as "RutCliente",
    det."Cantidad" as "Cantidad",
    det."Monto",
    '01' as "TipoTransaccion",
    comp."IdTipoOperacion" as "IdTipoOperacion",
    comp."IdTipoProducto" as "IdTipoProducto",
    row_number() over (order by "IdComprobanteDetalle") as "IdBaseDocumento",
    comp."IdCliente",
    comp."IdPortafolio"
from "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" as comp,
    "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" as det
left outer join "BSCL_VIRTUAL"."RUE_TBASSOC" as soc on ((ltrim(rtrim(det."NemotecnicoInstrumento")) = ltrim(rtrim(soc."NEMO_SASA")))),
    "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as cli,
    "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as per,
    "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as doc
where comp."IdComprobante" = det."IdComprobante"
and comp."IdCliente" = cli."IdCliente"
and cli."IdPersona" = per."IdPersona"
and per."IdDocumentoIdentidad" = doc."IdDocumentoIdentidad";

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;
