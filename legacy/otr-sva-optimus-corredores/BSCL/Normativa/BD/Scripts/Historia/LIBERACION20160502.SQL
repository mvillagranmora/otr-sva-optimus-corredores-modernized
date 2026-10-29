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
-- LIBERACION: 02-05-2016
--

--################################################
--       fpino.<MOTIVO>
--################################################

-- Actualiza cambios en el modelo.

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

drop table "BSCL.Normativa.BD::TRI.ResumenCarga";
drop table "BSCL.Normativa.BD::TRI.ResumenCuadratura";
drop table "BSCL.Normativa.BD::TRI.DatosDJ1885";
drop table "BSCL.Normativa.BD::TRI.DatosDJ1891";
drop table "BSCL.Normativa.BD::TRI.DetalleValidacion";
drop table "BSCL.Normativa.BD::TRI.ResumenValidacion";
drop table "BSCL.Normativa.BD::DOC.Documento";
drop table "BSCL.Normativa.BD::DOC.TipoDocumento";
drop table "BSCL.Normativa.BD::DOC.Validacion";
drop table "BSCL.Normativa.BD::DOC.Homologacion";
drop table "BSCL.Normativa.BD::DOC.Diccionario";
drop table "BSCL.Normativa.BD::DOC.CampoAgrupador";
drop table "BSCL.Normativa.BD::DOC.Agrupador";
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

    -- Fecha desde la cual se presentan los datos (inclusive).
    "FechaDatosDesde" date not null,

    -- Fecha hasta la cual se presentan los datos (inclusive).
    "FechaDatosHasta" date not null,

    primary key ("Esquema", "Version")
);

-- Table: "BSCL.Normativa.BD::DOC.Campo"
-- Especificacion de los campos del detalle de un documento,
-- en funcion de su representacion y formatos para el tratamiento
-- generico de los mismos en los programas correspondientes.
create column table "BSCL.Normativa.BD::DOC.Campo" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Nombre del campo (debe ser unico para esquema/version).
    "Nombre" nvarchar(40) not null,

    -- Tipo de diccionario relacionado.
    "TipoDiccionario" nvarchar(40) not null default '',

    -- Orden de presentacion del campo.
    "Orden" smallint not null,

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

    -- Indicador de si el campo es oculto en la grilla (S/N).
    "OcultoEnGrilla" char(1) not null,

    -- Indicador de si el campo es oculto en el mantenedor (S/N).
    "OcultoEnMantenedor" char(1) not null,

    -- Indicador de si la columna es comun (S/N).
    "Comun" char(1) not null,

    -- Indicador de si la columna es clave (S/N).
    "Clave" char(1) not null,

    -- Grupo de la columna.
    "Grupo" nvarchar(100) not null,

    -- Posición de disposicion del campo en archivo de carga.
    "OrdenCargaExterna" smallint not null,

    primary key ("Esquema", "Version", "Nombre")
);

alter table "BSCL.Normativa.BD::DOC.Campo"
   add constraint "FK_Campo_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Agrupador"
-- Especificacion de las campos agrupacion de los detalles de un documento.
create column table "BSCL.Normativa.BD::DOC.Agrupador" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Nombre del agrupador.
    "Agrupador" nvarchar(20) not null,

    -- Nombre del campo.
    "Descripcion" nvarchar(80) not null,

    primary key ("Esquema", "Version", "Agrupador")
);

alter table "BSCL.Normativa.BD::DOC.Agrupador"
   add constraint "FK_Agrupador_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.CampoAgrupador"
-- Especificacion de las campos agrupacion de los detalles de un documento.
create column table "BSCL.Normativa.BD::DOC.CampoAgrupador" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Nombre del agrupador.
    "Agrupador" nvarchar(40) not null,

    -- Nombre del campo.
    "NombreCampo" nvarchar(40) not null,

    -- Orden del campo en el agrupador.
    "Orden" smallint not null,

    primary key ("Esquema", "Version", "Agrupador", "NombreCampo")
);

alter table "BSCL.Normativa.BD::DOC.CampoAgrupador"
   add constraint "FK_CampoAgrupador_Agrupador" foreign key ("Esquema", "Version", "Agrupador")
      references "BSCL.Normativa.BD::DOC.Agrupador" ("Esquema", "Version", "Agrupador")
      on delete restrict on update restrict;

alter table "BSCL.Normativa.BD::DOC.CampoAgrupador"
   add constraint "FK_CampoAgrupador_Campo" foreign key ("Esquema", "Version", "NombreCampo")
      references "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Diccionario"
-- Definicion de codigos utilizados en la edicion y presentacion del documento.
-- Sirve para llenar combos.
-- La nomenclatura o codigo es la impuesta por la entidad reguladora.
create column table "BSCL.Normativa.BD::DOC.Diccionario" (
    -- Id del registro.
    "IdDiccionario" bigint not null,

--    -- Nombre del esquema.
--    "Esquema" nvarchar(10) not null,

--    -- Version del esquema.
--    "Version" nvarchar(10) not null,

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

-- Table: "BSCL.Normativa.BD::DOC.Homologacion"
-- Contiene homologación de tipos.
-- Cuando se cargan datos desde un origen operacional se debe aplicar
-- conversiones a los tipos de datos para dejar en formato segun de
-- acuerdo a los codigos definidos por la entidad reguladora.
create column table "BSCL.Normativa.BD::DOC.Homologacion" (
    -- Id del registro.
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
   add constraint "FK_Homologacion_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Validacion"
-- Definición de las validaciones aplicadas a los documentos.
create column table "BSCL.Normativa.BD::DOC.Validacion" (
    -- Id del registro.
    "IdValidacion" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Codigo de identificacion de la validacion (Ej: H1520).
    "Codigo" nvarchar(40) not null,

    -- Tipo de validacion (Ej: NORMA, CUADRATURA, NEGOCIO).
    "Tipo" nvarchar(40) not null,

    -- Indicador de la regla esta activa (S/N).
    "Activa" char(1) not null,

    -- Descripcion de la validacion.
    -- Ej: Si Tipo de Fondo es igual a F, entonces Tipo de Operacion debe ser igual a 1.
    "Descripcion" nvarchar(250) not null,

    -- Severidad de la validacion.
    -- "A" Advertencia / "E" Error.
    "Severidad" char(1) not null,

    -- Expresion de la regla de validacion.
    -- Ej. Si (C2) = F -> (C5) = 1
    "Formula" nvarchar(250) not null,

    primary key ("IdValidacion")
);

alter table "BSCL.Normativa.BD::DOC.Validacion"
   add constraint "FK_Validacion_Esquema" foreign key ("Esquema", "Version")
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
    -- Id del registro.
    "IdDocumento" bigint not null,

    -- Tipo de documento.
    "IdTipoDocumento" nvarchar(10) not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Id de la empresa.
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
    
    -- Indica si el documento esta en proceso
    "EnProceso" char(1) not null,

    -- Timestamp del inicio del ultimo proceso.
    "FechaInicioUltimoProceso" datetime null,

    primary key ("IdDocumento")
);

alter table "BSCL.Normativa.BD::DOC.Documento"
    add constraint "FK_Documento_TipoDocumento" foreign key ("IdTipoDocumento")
        references "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento")
        on delete restrict on update restrict;

alter table "BSCL.Normativa.BD::DOC.Documento"
    add constraint "FK_Documento_Esquema" foreign key ("Esquema", "Version")
        references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
        on delete restrict on update restrict;

-- Table: "BSCL"."BSCL.Normativa.BD::TRI.ResumenValidacion"
-- Contiene los resumenes de validacion del documento.
create column table "BSCL.Normativa.BD::TRI.ResumenValidacion" (
    -- Id del registro.
    "ID" bigint not null,

    -- Id del documento.
    "IdDocumento" bigint not null,

    -- Id del proceso.
    "IdProceso" bigint not null,

    -- Id de la validacion.
    "IdValidacion" bigint not null,

    -- Codigo de la validacion (Ej: H1520).
    "CodigoValidacion" nvarchar(40) not null,

    -- Descripcion de la validacion.
    "DescripcionValidacion" nvarchar(250) not null,

    -- Id de la validacion.
    "IdUsuario" bigint not null,

    -- Cantidad de casos en que no se cumple la regla.
    "CantidadCasos" bigint not null,

    -- Fecha de la validacion.
    "FechaValidacion" date not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.ResumenValidacion"
    add constraint "FK_ResumenValidacion_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete cascade on update restrict;

alter table "BSCL.Normativa.BD::TRI.ResumenValidacion"
    add constraint "FK_ResumenValidacion_Validacion" foreign key ("IdValidacion")
        references "BSCL.Normativa.BD::DOC.Validacion" ("IdValidacion")
        on delete cascade on update restrict;

-- Table: "BSCL"."BSCL.Normativa.BD::TRI.DetalleValidacion"
-- Contiene los detalles de validacion del documento.
create column table "BSCL.Normativa.BD::TRI.DetalleValidacion" (
    -- Id del registro.
    "ID" bigint not null,

    -- Id del resumen de validacion.
    "IdResumenValidacion" bigint not null,

    -- Id del detalle del documento.
    "IdDetalleDocumento" bigint not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DetalleValidacion"
    add constraint "FK_DetalleValidacion_ResumenValidacion" foreign key ("IdResumenValidacion")
        references "BSCL.Normativa.BD::TRI.ResumenValidacion" ("ID")
        on delete cascade on update restrict;

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
    add constraint "FK_DatosDJ1891_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete restrict on update restrict;

create index "IX01_DocumentoDJ1891" on "BSCL.Normativa.BD::TRI.DatosDJ1891"
    ("IdDocumento", "TipoDocumento", "NumeroDocumento");

create column table "BSCL.Normativa.BD::TRI.ResumenCarga" (
    -- Id de la auditoria.
    "ID" bigint not null,

    -- Id del documento.
    "IdProceso" bigint not null,

    -- Id del proceso.
    "IdDocumento" bigint not null,

    -- Id del usuario.
    "IdUsuario" bigint not null,

    -- Fecha/Hora de la auditoria.
    "Fecha" datetime not null,

    -- Glosa de la auditoria.
    "Glosa" nvarchar(200) not null,

    -- Nombre del archivo.
    "NombreArchivo" nvarchar(200) null,

    -- Total de registros leidos.
    "TotalRegistrosLeidos" bigint not null,

    -- Total de registros cargados.
    "TotalRegistrosCargados" bigint not null,

    -- Total de registros erroneos.
    "TotalRegistrosErroneos" bigint not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.ResumenCarga"
    add constraint "FK_ResumenCarga_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete cascade on update restrict;

create column table "BSCL.Normativa.BD::TRI.ResumenCuadratura" (
    -- Id de la auditoria.
    "ID" bigint not null,

    -- Id del documento.
    "IdProceso" bigint not null,

    -- Id del proceso.
    "IdDocumento" bigint not null,

    -- Id del usuario.
    "IdUsuario" bigint not null,

    -- Fecha/Hora de la auditoria.
    "Fecha" datetime not null,

    -- Glosa de la auditoria.
    "Glosa" nvarchar(200) not null,

    -- Total de registros destino.
    "TotalRegistrosDestino" bigint not null,

    -- Total de registros origen.
    "TotalRegistrosOrigen" bigint not null,

    -- Total de registros faltantes.
    "TotalRegistrosFaltantes" bigint not null,

    -- Total de registros sobrantes.
    "TotalRegistrosSobrantes" bigint not null,

    -- Total de registros iguales.
    "TotalRegistrosIguales" bigint not null,

    -- Total de registros diferentes.
    "TotalRegistrosDiferentes" bigint not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.ResumenCuadratura"
    add constraint "FK_ResumenCuadratura_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete cascade on update restrict;

------------------------------------------------------------------------------
-- Table: "BSCL.Normativa.BD::TRI.DatosDJ1885"
-- Contiene los datos de la DatosDJ1885.
-- Permite el ingreso de datos nulos, pues no en todas las
-- ediciones del documento se informan todos los campos o
-- existen campos condicionados entre si.
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

    -- SISTEMA / EXTERNA.
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

    -- RECIBIDO/PAGADO
    "TipoRegistro" nvarchar(1) not null,

    -- Nemotecnico del instrumento.
    "Nemotecnico" nvarchar(100) null,

    -- Fecha de Pago del Dividendo.
    "FechaPago" date null,

    -- RUT del emisor.
    "SecuenciaEvento" bigint null,

    -- RUT del Sociedad Pagadora.
    "RutSociedad" nvarchar(40) null,

    "RazonSocial" nvarchar(100) null,

    -- Tipo del Sociedad Pagadora: ABIERTA, CERRADA
    "TipoSociedad" nvarchar(40) null,

    -- Cantidad de acciones transadas.
    "CantidadAcciones31" bigint not null,

    "MontoPagado" decimal(26,8) not null,

    "MontoRecibido" decimal(26,8) not null,

    -- Monto.
    "AfectosIGCoAdicional" decimal(26,8) not null,
    "ExentoIGC" decimal(26,8) not null,
    "NoRentaNoAcogidos" decimal(26,8) not null,
    "NoRentaSiAcogidos" decimal(26,8) not null,
    "Incremento" decimal(26,8) not null,
    "CreditosParaIGCAfectosConDev" decimal(26,8) not null,
    "CreditosParaIGCAfectosSinDev" decimal(26,8) not null,
    "CreditosParaIGCExentosConDev" decimal(26,8) not null,
    "CreditosParaIGCExentosSinDev" decimal(26,8) not null,
    "CreditosPorImpuestoExternos" decimal(26,8) not null,
    "ImpuestoTasaAdicional" decimal(26,8) not null,
    "NumeroCertificado" bigint not null,

    -- Interpretación respecto al tipo Operacion Original.
    "CantidadDerecho"           decimal(26,8) not null,
    "IdInstrumento"             bigint not null,
    "IdEventoCapitalCliente"    bigint not null,

    -- CLT, COR, CIA, BOL, CAM, CUS
    "TipoSocioNegocio"          NVARCHAR(1) not null,
    "TipoCartera"               NVARCHAR(3) not null,
    "IdMoneda"                  NVARCHAR(3) not null,
    "TipoEventoCapital"         NVARCHAR(3) not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DatosDJ1885"
    add constraint "FK_Documento_DatosDJ1885" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete restrict on update restrict;

create index "IX01_DocumentoDJ1885" on "BSCL.Normativa.BD::TRI.DatosDJ1885"
    ("IdDocumento", "TipoInstrumento", "SecuenciaEvento");

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1885";
delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1891";
delete from "BSCL"."BSCL.Normativa.BD::TRI.ResumenCarga";
delete from "BSCL"."BSCL.Normativa.BD::TRI.ResumenCuadratura";
delete from "BSCL"."BSCL.Normativa.BD::TRI.ResumenValidacion";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Documento";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Validacion";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Homologacion";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Diccionario";
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoAgrupador";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Agrupador";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema";
delete from "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento";

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
/* DICCIONARIOS                                                 */
/*==============================================================*/

-- Tipo de Producto
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  1, 'TipoProducto',    'INAX',         1, 'Acciones extranjeras en BCS'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  2, 'TipoProducto',    'INAC',         2, 'Intermediacion acciones'      , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  3, 'TipoProducto',    'INCI',         3, 'Intermediacion CFI'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  4, 'TipoProducto',    'INCM',         4, 'Intermediacion CFM'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  5, 'TipoProducto',    'INCX',         5, 'Intermedacion CFI Extranjeros', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  6, 'TipoProducto',    'INEN',         6, 'Intermediacion ETF nacional'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  7, 'TipoProducto',    'INEX',         7, 'Intermediacion ETF extranjero', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  8, 'TipoProducto',    'INFU',         8, 'Intermediacion futuros'       , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select  9, 'TipoProducto',    'INFM',         9, 'Intermediacion fondos mutuos' , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 10, 'TipoProducto',    'INIF',        10, 'Intermediacion IIF'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 11, 'TipoProducto',    'INRF',        11, 'Intermediacion renta fija'    , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 12, 'TipoProducto',    'INSI',        12, 'Intermediacion simultaneas'   , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 13, 'TipoProducto',    'MEME',        13, 'Mesa moneda extranjera (Spot)', 'S', '', 'MonedaExtranjera' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 14, 'TipoProducto',    'MERF',        14, 'Mesa pactos'                  , 'S', '', 'MesaPactos'       from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 15, 'TipoProducto',    'INMO',        15, 'Intermediacion Monetarios'    , 'S', '', 'TipoProducto'     from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 16, 'TipoProducto',    'INST',        16, 'Intermediacion Stocks'        , 'S', '', 'TipoProducto'     from dummy;

-- Tipo de Transaccion
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 17, 'TipoTransac',     '00',           1, 'Comisiones                                                                                         ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 18, 'TipoTransac',     '01',           2, 'Acciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 19, 'TipoTransac',     '02',           3, 'Dolares                                                                                            ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 20, 'TipoTransac',     '03',           4, 'Oro                                                                                                ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 21, 'TipoTransac',     '04',           5, 'Otras Operaciones de Futuro                                                                        ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 22, 'TipoTransac',     '05',           6, 'Opciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 23, 'TipoTransac',     '06',           7, 'Documentos de Renta Fija                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 24, 'TipoTransac',     '07',           8, 'Titulos Emitidos por Bolsas de Productos Agropecuarios sobre Certificados de Depositos de Productos', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 25, 'TipoTransac',     '08',           9, 'Cuotas de Fondos de Inversion                                                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 26, 'TipoTransac',     '09',          10, 'Otros Instrumentos de Inversion                                                                    ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 27, 'TipoTransac',     '10',          11, 'Aporte y Adquisicion de Valores (ETF - Exchange Traded Funds)                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 28, 'TipoTransac',     '99',          12, 'Otros que no sean instrumentos de inversi�n                                                        ', 'S', '', '' from dummy;

-- Tipo de Documento
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 29, 'TipoDocu',        'C',            1, 'Nota de Credito',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 30, 'TipoDocu',        'D',            2, 'Nota de Debito ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 31, 'TipoDocu',        'F',            3, 'Factura        ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 32, 'TipoDocu',        'O',            4, 'Otro           ',           'S', '', 'Facturacion' from dummy;

-- Origen de los Datos
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 33, 'Origen',          'CARGAEXT',     1, 'Carga Masiva Externa ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 34, 'Origen',          'INGMANUAL',    2, 'Ingreso Manual       ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 35, 'Origen',          'OPERACIONAL',  3, 'Sebra CB             ',     'S', '', ''            from dummy;

-- Tipo de Operacion
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 36, 'TipoOperacion',    'CO',           1, 'Compra           ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 37, 'TipoOperacion',    'CP',           2, 'Compra con Pacto ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 38, 'TipoOperacion',    'CT',           3, 'Compra T�rmino   ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 39, 'TipoOperacion',    'VE',           4, 'Venta            ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 40, 'TipoOperacion',    'VP',           5, 'Venta con Pacto  ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 41, 'TipoOperacion',    'VT',           6, 'Venta Termino    ',         'S', '', ''            from dummy;

-- VoBo del Usuario
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 42, 'VistoBueno',      'N',            1, 'No Revisados por Usuario ', 'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 43, 'VistoBueno',      'S',            2, 'Revisados por Usuario    ', 'S', '', ''            from dummy;

-- Tipo de Evento de Capital
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 44, 'TipoEventoCapital',    'DV',           1, 'Dividendos Acciones  ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 45, 'TipoEventoCapital',    'RE',           2, 'Reparto',         'S', '', ''            from dummy;

-- Tipo de Sociedad
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 46, 'TipoSociedad',    'A',            1, 'Sociedad Anonima Abierta',                  'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 47, 'TipoSociedad',    'C',            2, 'Sociedad Anonima Cerrada',                  'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 48, 'TipoSociedad',    'E',            3, 'Sociedad en Comandita por Acciones',     'S', '', ''            from dummy;

-- Tipo de Registro
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 49, 'TipoRegistro',    'P',            1, 'Pagado',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 50, 'TipoRegistro',    'R',            2, 'Recibido',       'S', '', ''            from dummy;

-- Tipo de Instrumento
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("IdDiccionario", "Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 51, 'TipoInstrumento', 'ACN',          1, 'Acciones Nacionales',       'S', '', ''            from dummy;

/*==============================================================*/
/* PARAMETROS DE CONFIGURACION                                  */
/*==============================================================*/

delete from "BSCL.Plataforma.BD::PLA.Configuracion"
where "IdEmpresa"=1 and "CodigoParametro"='RaizArchivos';
insert into "BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
values(1, 'RaizArchivos', 'Ruta raiz de los archivos', 'C:/files');

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1891";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Validacion" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoAgrupador" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Agrupador" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" where "Esquema" = 'DJ1891';

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
    '2015-03-30' as "FechaPresentacionLimite",
    '2014-01-01' as "FechaDatosDesde",
    '2014-12-31' as "FechaDatosHasta"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '1996-02-15' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2016-03-30' as "FechaPresentacionLimite",
    '2015-01-01' as "FechaDatosDesde",
    '2015-12-31' as "FechaDatosHasta"
from dummy;

/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    1 as "IdValidacion",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'H115' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'N° de documento > 0' as "Descripcion",
    'E' as "Severidad",
    'N° de documento > 0' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    2 as "IdValidacion",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'H116' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Tipo Documento= {F .o. C .o. D .o. O}' as "Descripcion",
    'E' as "Severidad",
    'Tipo Documento= {F .o. C .o. D .o. O}' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    3 as "IdValidacion",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'H117' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Tipo Transacción={0.o.1.o.2.o. 3.o. 4.o. 5.o. 6.o. 7.o.8.o.9 .o.10.o. 99}' as "Descripcion",
    'E' as "Severidad",
    'Tipo Transacción={0.o.1.o.2.o. 3.o. 4.o. 5.o. 6.o. 7.o.8.o.9 .o.10.o. 99}' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    4 as "IdValidacion",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'H1350' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si C8 = 1.o. C8 = 8, entonces [C13] <> blanco. O. [C13] > 0' as "Descripcion",
    'E' as "Severidad",
    'Si C8 = 1.o. C8 = 8, entonces [C13] <> blanco. O. [C13] > 0' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    5 as "IdValidacion",
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'H611' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    '31.12.AT-2 < fecha < 01.01.AT .y. fecha <> Blanco' as "Descripcion",
    'E' as "Severidad",
    '31.12.AT-2 < fecha < 01.01.AT .y. fecha <> Blanco' as "Formula"
from dummy;

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/

--
-- DJ1891-2015
--
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'ID' as "Nombre",
    '' as "TipoDiccionario",
    1 as "Orden",
    'Secuencia' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'RutCliente' as "Nombre",
    '' as "TipoDiccionario",
    11 as "Orden",
    'RUT Cliente' as "Titulo",
    'RUT_CLIENTE' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '' as "Grupo",
    9 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'IdDocumento' as "Nombre",
    '' as "TipoDiccionario",
    2 as "Orden",
    'Secuencia Docto' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'S' as "OcultoEnGrilla",
    'S' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'VboUsuario' as "Nombre",
    '' as "TipoDiccionario",
    3 as "Orden",
    'VoBo' as "Titulo",
    'TEXT' as "TipoDato",
    1 as "Largo",
    'N' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'Observaciones' as "Nombre",
    '' as "TipoDiccionario",
    4 as "Orden",
    'Observaciones' as "Titulo",
    'TEXT' as "TipoDato",
    250 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'FuenteDatos' as "Nombre",
    '' as "TipoDiccionario",
    5 as "Orden",
    'Fuente' as "Titulo",
    'TEXT' as "TipoDato",
    0 as "Largo",
    'INICIAL' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'TipoDocumento' as "Nombre",
    'TipoDocu' as "TipoDiccionario",
    6 as "Orden",
    'Tipo Documento' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'S' as "Clave",
    '1. Datos' as "Grupo",
    1 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'NumeroDocumento' as "Nombre",
    '' as "TipoDiccionario",
    7 as "Orden",
    'Número Documento' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'S' as "Clave",
    '1. Datos' as "Grupo",
    3 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'FechaDocumento' as "Nombre",
    '' as "TipoDiccionario",
    8 as "Orden",
    'Fecha Documento' as "Titulo",
    'DATE_FILTER' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    2 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'RutEmisor' as "Nombre",
    '' as "TipoDiccionario",
    9 as "Orden",
    'RUT Emisor' as "Titulo",
    'RUT' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    11 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'Nemotecnico' as "Nombre",
    '' as "TipoDiccionario",
    10 as "Orden",
    'Instrumento' as "Titulo",
    'NEMO' as "TipoDato",
    100 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    5 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'Cantidad' as "Nombre",
    '' as "TipoDiccionario",
    12 as "Orden",
    'Cantidad' as "Titulo",
    'QTY' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    7 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'Monto' as "Nombre",
    '' as "TipoDiccionario",
    13 as "Orden",
    'Monto' as "Titulo",
    'AMOUNT' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    8 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'TipoTransaccion' as "Nombre",
    'TipoTransac' as "TipoDiccionario",
    14 as "Orden",
    'Tipo Transacción' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    12 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'IdTipoOperacion' as "Nombre",
    'TipoOperacion' as "TipoDiccionario",
    15 as "Orden",
    'Tipo Operación' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    6 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'IdTipoProducto' as "Nombre",
    'TipoProducto' as "TipoDiccionario",
    16 as "Orden",
    'Tipo Producto' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'IdBaseDocumento' as "Nombre",
    '' as "TipoDiccionario",
    17 as "Orden",
    'Secuencia Base' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'S' as "Clave",
    '1. Datos' as "Grupo",
    4 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'IdCliente' as "Nombre",
    '' as "TipoDiccionario",
    18 as "Orden",
    'Secuencia Cliente' as "Titulo",
    'ID_CLIENTE' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'IdPortafolio' as "Nombre",
    '' as "TipoDiccionario",
    19 as "Orden",
    'Secuencia Portafolio' as "Titulo",
    'ID_PORTAFOLIO' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;

--
-- DJ1891-2016
--
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'ID' as "Nombre",
    '' as "TipoDiccionario",
    1 as "Orden",
    'Secuencia' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'RutCliente' as "Nombre",
    '' as "TipoDiccionario",
    11 as "Orden",
    'RUT Cliente' as "Titulo",
    'RUT_CLIENTE' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'IdDocumento' as "Nombre",
    '' as "TipoDiccionario",
    2 as "Orden",
    'Secuencia Docto' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'S' as "OcultoEnGrilla",
    'S' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'VboUsuario' as "Nombre",
    '' as "TipoDiccionario",
    3 as "Orden",
    'VoBo' as "Titulo",
    'TEXT' as "TipoDato",
    1 as "Largo",
    'N' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'Observaciones' as "Nombre",
    '' as "TipoDiccionario",
    4 as "Orden",
    'Observaciones' as "Titulo",
    'TEXT' as "TipoDato",
    250 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'S' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'FuenteDatos' as "Nombre",
    '' as "TipoDiccionario",
    5 as "Orden",
    'Fuente' as "Titulo",
    'TEXT' as "TipoDato",
    40 as "Largo",
    'INICIAL' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'S' as "Comun",
    'N' as "Clave",
    '2. Control' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'TipoDocumento' as "Nombre",
    'TipoDocu' as "TipoDiccionario",
    6 as "Orden",
    'Tipo Documento' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'S' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'NumeroDocumento' as "Nombre",
    '' as "TipoDiccionario",
    7 as "Orden",
    'Número Documento' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'S' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'FechaDocumento' as "Nombre",
    '' as "TipoDiccionario",
    8 as "Orden",
    'Fecha Documento' as "Titulo",
    'DATE_FILTER' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'RutEmisor' as "Nombre",
    '' as "TipoDiccionario",
    9 as "Orden",
    'RUT Emisor' as "Titulo",
    'RUT' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'Nemotecnico' as "Nombre",
    '' as "TipoDiccionario",
    10 as "Orden",
    'Instrumento' as "Titulo",
    'NEMO' as "TipoDato",
    40 as "Largo",
    '' as "ValorDefecto",
    'N' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'Cantidad' as "Nombre",
    '' as "TipoDiccionario",
    12 as "Orden",
    'Cantidad' as "Titulo",
    'QTY' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'Monto' as "Nombre",
    '' as "TipoDiccionario",
    13 as "Orden",
    'Monto' as "Titulo",
    'AMOUNT' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'TipoTransaccion' as "Nombre",
    'TipoTransac' as "TipoDiccionario",
    14 as "Orden",
    'Tipo Transacción' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'IdTipoOperacion' as "Nombre",
    'TipoOperacion' as "TipoDiccionario",
    15 as "Orden",
    'Tipo Operación' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'IdTipoProducto' as "Nombre",
    'TipoProducto' as "TipoDiccionario",
    16 as "Orden",
    'Tipo Producto' as "Titulo",
    'NEMO' as "TipoDato",
    0 as "Largo",
    '' as "ValorDefecto",
    'S' as "Obligatorio",
    'S' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'IdBaseDocumento' as "Nombre",
    '' as "TipoDiccionario",
    17 as "Orden",
    'Secuencia Base' as "Titulo",
    'ID' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'N' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'S' as "Clave",
    '1. Datos' as "Grupo",
    4 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'IdCliente' as "Nombre",
    '' as "TipoDiccionario",
    18 as "Orden",
    'Secuencia Cliente' as "Titulo",
    'ID_CLIENTE' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'IdPortafolio' as "Nombre",
    '' as "TipoDiccionario",
    19 as "Orden",
    'Secuencia Portafolio' as "Titulo",
    'ID_PORTAFOLIO' as "TipoDato",
    0 as "Largo",
    '0' as "ValorDefecto",
    'S' as "Obligatorio",
    'N' as "Editable",
    'N' as "ModoMasivo",
    'S' as "Exportable",
    'N' as "OcultoEnGrilla",
    'N' as "OcultoEnMantenedor",
    'N' as "Comun",
    'N' as "Clave",
    '1. Datos' as "Grupo",
    0 as "OrdenCargaExterna"
from dummy;

/*==============================================================*/
/* AGRUPADORES                                                  */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Agrupador"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'PorRutCliente' as "Agrupador",
    'Agrupacion por RUT Cliente' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Agrupador"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'PorRutCliente' as "Agrupador",
    'Agrupacion por RUT Cliente' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoAgrupador"
select
    'DJ1891' as "Esquema",
    '2015' as "Version",
    'PorRutCliente' as "Agrupador",
    'RutCliente' as "NombreCampo",
    1 as "Orden"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoAgrupador"
select
    'DJ1891' as "Esquema",
    '2016' as "Version",
    'PorRutCliente' as "Agrupador",
    'RutCliente' as "NombreCampo",
    1 as "Orden"
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
    1 as "IdTitularDocumento",
    '90947000-1' as "DocumentoIdentidadTitular",
    2 as "IdRepresentanteLegal",
    '14448678-1' as "DocumentoIdentidadRepLegal",
    'EL Golf 47' as "DireccionFisicaTitular",
    'contabilidad@imtrust.cl' as "DireccionElectronicaTitular",
    '56 2 25689854' as "TelefonoTitular",
    'I9153103' as "FolioPresentacionDocumento",
    'N' as "EnProceso",
    null as "EnProcesoDesde"
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
    'INICIAL' as "FuenteDatos",
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
left outer join "BSCL_VIRTUAL"."TBASSOC" as soc on ((ltrim(rtrim(det."NemotecnicoInstrumento")) = ltrim(rtrim(soc."NEMO_SASA")))),
    "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as cli,
    "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as per,
    "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as doc
where comp."IdComprobante" = det."IdComprobante"
and comp."IdCliente" = cli."IdCliente"
and cli."IdPersona" = per."IdPersona"
and per."IdDocumentoIdentidad" = doc."IdDocumentoIdentidad";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1885";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Validacion" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoAgrupador" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Agrupador" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" where "Esquema" = 'DJ1885';

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'DJ1885' as "Esquema",
    '2015' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '1996-02-15' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2015-03-26' as "FechaPresentacionLimite",
    '2014-01-01' as "FechaDatosDesde",
    '2014-12-31' as "FechaDatosHasta"
from dummy;

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/
-- DJ1885-2015
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Campo"
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1885'

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'ID'                           as "Nombre", ''                  as "TipoDiccionario",  1 as "Orden", 'Secuencia'                                                  as "Titulo", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'S' as "Clave", ''                                                   as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'IdDocumento'                  as "Nombre", ''                  as "TipoDiccionario",  2 as "Orden", 'Secuencia Docto'                                            as "Titulo", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'S' as "Comun", 'S' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'VboUsuario'                   as "Nombre", 'VistoBueno'        as "TipoDiccionario",  3 as "Orden", 'VoBo'                                                       as "Titulo", 'TEXT'        as "TipoDato",   1 as "Largo", 'N'       as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'Observaciones'                as "Nombre", ''                  as "TipoDiccionario",  4 as "Orden", 'Observaciones'                                              as "Titulo", 'TEXT'        as "TipoDato", 250 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'FuenteDatos'                  as "Nombre", 'Origen'            as "TipoDiccionario",  5 as "Orden", 'Fuente'                                                     as "Titulo", 'TEXT'        as "TipoDato",   0 as "Largo", 'INICIAL' as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'RutCliente'                   as "Nombre", ''                  as "TipoDiccionario",  6 as "Orden", 'Rut Cliente'                                                as "Titulo", 'RUT_CLIENTE' as "TipoDato",  40 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ''                                                   as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'IdCliente'                    as "Nombre", ''                  as "TipoDiccionario",  7 as "Orden", 'Secuencia Cliente'                                          as "Titulo", 'ID_CLIENTE'  as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'IdPortafolio'                 as "Nombre", ''                  as "TipoDiccionario",  8 as "Orden", 'Portafolio Cliente'                                         as "Titulo", 'NEMO'        as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'TipoInstrumento'              as "Nombre", 'TipoInstrumento'   as "TipoDiccionario",  9 as "Orden", 'Tipo Instrumento'                                           as "Titulo", 'NEMO'        as "TipoDato",  40 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'TipoRegistro'                 as "Nombre", 'TipoRegistro'      as "TipoDiccionario", 10 as "Orden", 'Recibido/Pagado'                                            as "Titulo", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'Nemotecnico'                  as "Nombre", ''                  as "TipoDiccionario", 11 as "Orden", 'Nemotecnico'                                                as "Titulo", 'NEMO'        as "TipoDato", 100 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'FechaPago'                    as "Nombre", ''                  as "TipoDiccionario", 12 as "Orden", 'Fecha de Pago'                                              as "Titulo", 'DATE_FILTER' as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'SecuenciaEvento'              as "Nombre", ''                  as "TipoDiccionario", 13 as "Orden", 'Secuencia Evento'                                           as "Titulo", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'RutSociedad'                  as "Nombre", ''                  as "TipoDiccionario", 14 as "Orden", 'RUT del Pagador '                                           as "Titulo", 'RUT'         as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'RazonSocial'                  as "Nombre", ''                  as "TipoDiccionario", 15 as "Orden", 'Razon Social'                                               as "Titulo", 'NEMO'        as "TipoDato",   0 as "Largo", '100'     as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'TipoSociedad'                 as "Nombre", 'TipoSociedad'      as "TipoDiccionario", 16 as "Orden", 'Tipo Sociedad'                                              as "Titulo", 'NEMO'        as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'CantidadAcciones31'           as "Nombre", ''                  as "TipoDiccionario", 17 as "Orden", 'Cantidad de Acciones al 31/12'                              as "Titulo", 'QTY'         as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'MontoPagado'                  as "Nombre", ''                  as "TipoDiccionario", 18 as "Orden", 'Monto Pagado'                                               as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'MontoRecibido'                as "Nombre", ''                  as "TipoDiccionario", 19 as "Orden", 'Monto Recibido'                                             as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'AfectosIGCoAdicional'         as "Nombre", ''                  as "TipoDiccionario", 20 as "Orden", 'Afectos a IGC o Adicional'                                  as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'ExentoIGC'                    as "Nombre", ''                  as "TipoDiccionario", 21 as "Orden", 'Exentos de IGC'                                             as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'NoRentaNoAcogidos'            as "Nombre", ''                  as "TipoDiccionario", 22 as "Orden", 'No Constitutivo de Renta No Acogido a Impuesto Sustitutivo' as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'NoRentaSiAcogidos'            as "Nombre", ''                  as "TipoDiccionario", 23 as "Orden", 'No Constitutivo de Renta Acogido a Impuesto Sustitutivo'    as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'Incremento'                   as "Nombre", ''                  as "TipoDiccionario", 24 as "Orden", 'Incremento por Impuesto 1era. Categoría'                    as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'CreditosParaIGCAfectosConDev' as "Nombre", ''                  as "TipoDiccionario", 25 as "Orden", 'Afectos a IGC Con Devolucion'                               as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'CreditosParaIGCAfectosSinDev' as "Nombre", ''                  as "TipoDiccionario", 26 as "Orden", 'Afectos a IGC Sin Devolucion'                               as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'CreditosParaIGCExentosConDev' as "Nombre", ''                  as "TipoDiccionario", 27 as "Orden", 'Afectos a IGC Con Devolucion'                               as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'CreditosParaIGCExentosSinDev' as "Nombre", ''                  as "TipoDiccionario", 28 as "Orden", 'Afectos a IGC Sin Devolucion'                               as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'CreditosPorImpuestoExternos'  as "Nombre", ''                  as "TipoDiccionario", 29 as "Orden", 'Créditos por Impuestos Externos'                            as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'ImpuestoTasaAdicional'        as "Nombre", ''                  as "TipoDiccionario", 30 as "Orden", 'ImpuestoTasaAdicional'                                      as "Titulo", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'NumeroCertificado'            as "Nombre", ''                  as "TipoDiccionario", 31 as "Orden", 'Número de Certificado'                                      as "Titulo", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'CantidadDerecho'              as "Nombre", ''                  as "TipoDiccionario", 32 as "Orden", 'Cantidad Derecho'                                           as "Titulo", 'QTY'         as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'IdInstrumento'                as "Nombre", ''                  as "TipoDiccionario", 33 as "Orden", 'Secuencia Instrumento'                                      as "Titulo", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'IdEventoCapitalCliente'       as "Nombre", ''                  as "TipoDiccionario", 34 as "Orden", 'Secuencia Evento de Capital '                               as "Titulo", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'TipoSocioNegocio'             as "Nombre", ''                  as "TipoDiccionario", 35 as "Orden", 'Tipo Socio de Negocio'                                      as "Titulo", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'TipoCartera'                  as "Nombre", ''                  as "TipoDiccionario", 36 as "Orden", 'Tipo Cartera'                                               as "Titulo", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'IdMoneda'                     as "Nombre", ''                  as "TipoDiccionario", 37 as "Orden", 'Moneda '                                                    as "Titulo", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2015' as "Version", 'TipoEventoCapital'            as "Nombre", 'TipoEventoCapital' as "TipoDiccionario", 38 as "Orden", 'Tipo Evento Capital'                                        as "Titulo", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna" from dummy;

/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/

/*==============================================================*/
/* DOCUMENTOS                                                   */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Documento"
select
    2 as "IdDocumento",
    'DECLACORR' as "IdTipoDocumento",
    'DJ1885' as "Esquema",
    '2015' as "Version",
    1 as "IdEmpresa",
    '2016-03-26' as "FechaLimite",
    'ACT' as "IdEstadoDocumento",
    '2016-01-01' as "FechaCreacion",
    '2016-03-26' as "FechaPresentacion",
    1 as "IdTitularDocumento",
    '90947000-1' as "DocumentoIdentidadTitular",
    2 as "IdRepresentanteLegal",
    '14448678-1' as "DocumentoIdentidadRepLegal",
    'EL Golf 47' as "DireccionFisicaTitular",
    'contabilidad@imtrust.cl' as "DireccionElectronicaTitular",
    '56 2 25689854' as "TelefonoTitular",
    'I9153103' as "FolioPresentacionDocumento",
    'N' as "EnProceso",
    null as "EnProcesoDesde"
from dummy;

/*==============================================================*/
/* DETALLES DJ1885                                              */
/*==============================================================*/
insert into "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1885"
SELECT
    row_number() over (order by "IdEventoCapitalCliente") as ID,
    2 as "IdDocumento",
    'N' as "VboUsuario",
    '' as "Observaciones",
    'INICIAL' as "FuenteDatos",

    EvCli."DocumentoIdentidad",
    Portafolio."IdCliente",
    EvCli."IdPortafolio",
    EvCli."IdTipoInstrumento",
    'P',
    EvCli."Nemo",
    EvCli."FechaPago",
    EvCli."Secuencia",

    /*
    "A" si se trata de una Sociedad Anónima Abierta,
    "C" si se trata de una Sociedad Anónima Cerrada o Sociedades por Acciones
    "E" si se trata de una Sociedad en Comandita por Acciones)
    */

    Emisor."RutEmisor",
    Emisor."RazonSocial",
    'A' as "TipoSociedad" ,
    0 as "CantidadAcciones31",
    EvCli."Total" as "MontoPagado",
    0 as "MontoRecibido",
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    -- Datos de referencia
    EvCli."CantidadDerecho",
    EvCli."IdInstrumento",
    EvCli."IdEventoCapitalCliente",
    EvCli."TipoSocioNegocio",
    EvCli."TipoCartera",
    EvCli."IdMoneda",
    TipEvCap."Codigo"

FROM "BSCL"."BSCL.Custodia.BD::CUS.EventoCapitalCliente" as EvCli
    INNER JOIN "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio
        ON EvCli."IdPortafolio" = Portafolio."IdPortafolio"
    INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
        ON EvCli."IdInstrumento" = Instrumento."IdInstrumento"
    INNER JOIN "BSCL"."BSCL.Mercado.BD::MER.Emisor" AS Emisor
        ON Instrumento."IdEmisor" = Emisor."IdEmisor"
    INNER JOIN "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" AS TipEvCap
        ON EvCli."IdTipoEventoCapital" = TipEvCap."IdTipoEventoCapital"
where EvCli."IdTipoInstrumento" in ('ACN');

/*

76072469-6A

-- 76109779-2A
select * from "BSCL_VIRTUAL"."RUE_TBASSOC" where "RUT" = ' 76109779-2' Tipo S, Sociedad
select * from "BSCL_VIRTUAL"."RUE_TBUTEMIS" where "EMISOR" = 'MELON', TIPO_ENT = 1
-- 76116242-KA
select * from "BSCL_VIRTUAL"."RUE_TBASSOC" where "RUT" = ' 76116242-K' -- Tipo S, Sociedad
select * from "BSCL_VIRTUAL"."RUE_TBUTEMIS" where "EMISOR" = 'CFR' --, TIPO_ENT = 3

-- 76144216-3C
-- 76144163-9C
select * from "BSCL_VIRTUAL"."RUE_TBASSOC" where "RUT"  = ' 76144216-3'
select * from "BSCL_VIRTUAL"."RUE_TBUTEMIS" where "RUT" = ' 76144216-3'

select * from "BSCL"."BSCL.Mercado.BD::MER.Emisor"
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;
    