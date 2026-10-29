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
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INAX',         1, 'Acciones extranjeras en BCS'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INAC',         2, 'Intermediacion acciones'      , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INCI',         3, 'Intermediacion CFI'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INCM',         4, 'Intermediacion CFM'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INCX',         5, 'Intermedacion CFI Extranjeros', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INEN',         6, 'Intermediacion ETF nacional'  , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INEX',         7, 'Intermediacion ETF extranjero', 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INFU',         8, 'Intermediacion futuros'       , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INFM',         9, 'Intermediacion fondos mutuos' , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INIF',        10, 'Intermediacion IIF'           , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INRF',        11, 'Intermediacion renta fija'    , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INSI',        12, 'Intermediacion simultaneas'   , 'S', '', 'Facturacion'      from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'MEME',        13, 'Mesa moneda extranjera (Spot)', 'S', '', 'MonedaExtranjera' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'MERF',        14, 'Mesa pactos'                  , 'S', '', 'MesaPactos'       from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INMO',        15, 'Intermediacion Monetarios'    , 'S', '', 'TipoProducto'     from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoProducto',    'INST',        16, 'Intermediacion Stocks'        , 'S', '', 'TipoProducto'     from dummy;

-- Tipo de Transaccion
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '00',           1, 'Comisiones                                                                                         ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '01',           2, 'Acciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '02',           3, 'Dolares                                                                                            ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '03',           4, 'Oro                                                                                                ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '04',           5, 'Otras Operaciones de Futuro                                                                        ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '05',           6, 'Opciones                                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '06',           7, 'Documentos de Renta Fija                                                                           ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '07',           8, 'Titulos Emitidos por Bolsas de Productos Agropecuarios sobre Certificados de Depositos de Productos', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '08',           9, 'Cuotas de Fondos de Inversion                                                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '09',          10, 'Otros Instrumentos de Inversion                                                                    ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '10',          11, 'Aporte y Adquisicion de Valores (ETF - Exchange Traded Funds)                                      ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoTransac',     '99',          12, 'Otros que no sean instrumentos de inversi�n                                                        ', 'S', '', '' from dummy;

-- Tipo de Documento
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoDocu',        'C',            1, 'Nota de Credito',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoDocu',        'D',            2, 'Nota de Debito ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoDocu',        'F',            3, 'Factura        ',           'S', '', 'Facturacion' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoDocu',        'O',            4, 'Otro           ',           'S', '', 'Facturacion' from dummy;

-- Origen de los Datos
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'Origen',          'CARGAEXT',     1, 'Carga Masiva Externa ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'Origen',          'INGMANUAL',    2, 'Ingreso Manual       ',     'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'Origen',          'OPERACIONAL',  3, 'Sebra CB             ',     'S', '', ''            from dummy;

-- Tipo de Operacion
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacion',    'CO',           1, 'Compra           ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacion',    'CP',           2, 'Compra con Pacto ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacion',    'CT',           3, 'Compra T�rmino   ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacion',    'VE',           4, 'Venta            ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacion',    'VP',           5, 'Venta con Pacto  ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacion',    'VT',           6, 'Venta Termino    ',         'S', '', ''            from dummy;

-- VoBo del Usuario
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'VistoBueno',      'N',            1, 'No Revisados por Usuario ', 'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'VistoBueno',      'S',            2, 'Revisados por Usuario    ', 'S', '', ''            from dummy;

-- Tipo de Evento de Capital
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoEventoCapital',    'DV',           1, 'Dividendos Acciones  ',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoEventoCapital',    'RE',           2, 'Reparto',         'S', '', ''            from dummy;

-- Tipo de Sociedad
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoSociedad',    'A',            1, 'Sociedad Anonima Abierta',                  'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoSociedad',    'C',            2, 'Sociedad Anonima Cerrada',                  'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoSociedad',    'E',            3, 'Sociedad en Comandita por Acciones',     'S', '', ''            from dummy;

-- Tipo de Registro
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoRegistro',    'P',            1, 'Pagado',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoRegistro',    'R',            2, 'Recibido',       'S', '', ''            from dummy;

-- Tipo de Instrumento
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoInstrumento', 'ACN',          1, 'Acciones Nacionales',       'S', '', ''            from dummy;

-- Tipo de Fondos 
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoFondo',    'I',           1, 'Fondos de Inversión Públicos',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoFondo',    'F',           2, 'Fondos Mutuos',         'S', '', ''            from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoFondo',    'P',           3, 'Fondos de Inversion Privados',         'S', '', ''            from dummy;

-- Tipo de Operacion Eventos de Fondos
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacionFondos',    '1',           1, 'Reparto de dividendos, remesas o distribuciones de Fondos de Inversión o Fondos Mutuos', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacionFondos',    '2',           2, 'Liquidación del Fondo de Inversión', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacionFondos',    '3',           3, 'Disminución de capital del Fondo de Inversión', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacionFondos',    '4',           4, 'Rescate o enajenación de cuotas de Fondo de Inversión', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacionFondos',    '5',           5, 'Cantidades distribuidas a partícipes sin domicilio ni residencia en Chile','S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacionFondos',    '6',           6, 'Devoluciones de capital de Fondos de Inversión y Fondos Mutuos','S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoOperacionFondos',    '7',           7, 'Sin operaciones durante el ejercicio comercial','S', '', '' from dummy;

-- Unidad de Reajuste -- Homologar
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoReajuste',    '1',           1, 'Peso Chileno', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoReajuste',    '2',           2, 'Dólar Americano', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoReajuste',    '3',           3, 'Euro', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoReajuste',    '4',           4, 'Unidad de Fomento', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoReajuste',    '5',           5, 'IVP','S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'TipoReajuste',    '6',           6, 'Otro','S', '', '' from dummy;

-- Participe Cumple Requisitos
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'ParticipeCumpleRequisitos',    '1',           1, 'No tiene extranjero domiciliado con %5 participacion', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'ParticipeCumpleRequisitos',    '2',           2, 'Si tiene extranjero domiciliado con %5 participacion', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'ParticipeCumpleRequisitos',    '3',           3, 'No Aplica', 'S', '', '' from dummy;

-- Cuotas Cumplen Requisitos
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'CuotasCumplenRequisitos',    '1',           1, 'Cuotas cumplen requisitos Art. 82 ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'CuotasCumplenRequisitos',    '2',           2, 'Cuotas NO cumplen requisitos Art. 82 ', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'CuotasCumplenRequisitos',    '3',           3, 'No Aplica', 'S', '', '' from dummy;

-- Anexo Pais
--delete from "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" where  "Tipo" = 'PaisResidencia'
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AF' as "Codigo",'1' as "Orden",'Afganistán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AL' as "Codigo",'2' as "Orden",'Albania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'DE' as "Codigo",'3' as "Orden",'Alemania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AD' as "Codigo",'4' as "Orden",'Andorra' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AO' as "Codigo",'5' as "Orden",'Angola' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AI' as "Codigo",'6' as "Orden",'Anguila' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AQ' as "Codigo",'7' as "Orden",'Antártida' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AG' as "Codigo",'8' as "Orden",'Antigua y Barbuda' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SA' as "Codigo",'9' as "Orden",'Arabia Saudita' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'DZ' as "Codigo",'10' as "Orden",'Argelia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AR' as "Codigo",'11' as "Orden",'Argentina' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AM' as "Codigo",'12' as "Orden",'Armenia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AW' as "Codigo",'13' as "Orden",'Aruba' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AU' as "Codigo",'14' as "Orden",'Australia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AT' as "Codigo",'15' as "Orden",'Austria' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AZ' as "Codigo",'16' as "Orden",'Azerbaiyán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BS' as "Codigo",'17' as "Orden",'Bahamas' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BH' as "Codigo",'18' as "Orden",'Bahrein' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BD' as "Codigo",'19' as "Orden",'Bangladesh' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BB' as "Codigo",'20' as "Orden",'Barbados' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BE' as "Codigo",'21' as "Orden",'Bélgica' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BZ' as "Codigo",'22' as "Orden",'Belice' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BJ' as "Codigo",'23' as "Orden",'Benín' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BM' as "Codigo",'24' as "Orden",'Bermudas' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BY' as "Codigo",'25' as "Orden",'Bielorrusia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BO' as "Codigo",'26' as "Orden",'Bolivia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BQ' as "Codigo",'27' as "Orden",'Bonaire, San Eustaquio y Saba' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BA' as "Codigo",'28' as "Orden",'Bosnia y Herzegovina' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BW' as "Codigo",'29' as "Orden",'Botswana' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BR' as "Codigo",'30' as "Orden",'Brasil' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BN' as "Codigo",'31' as "Orden",'Brunéi Darussalam' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BG' as "Codigo",'32' as "Orden",'Bulgaria' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BF' as "Codigo",'33' as "Orden",'Burkina Faso' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BI' as "Codigo",'34' as "Orden",'Burundi' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BT' as "Codigo",'35' as "Orden",'Bután' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CV' as "Codigo",'36' as "Orden",'Cabo Verde' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KH' as "Codigo",'37' as "Orden",'Camboya' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CM' as "Codigo",'38' as "Orden",'Camerún' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CA' as "Codigo",'39' as "Orden",'Canadá' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TD' as "Codigo",'40' as "Orden",'Chad' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CL' as "Codigo",'41' as "Orden",'Chile' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CN' as "Codigo",'42' as "Orden",'China' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CY' as "Codigo",'43' as "Orden",'Chipre' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'VA' as "Codigo",'44' as "Orden",'Ciudad del Vaticano' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CO' as "Codigo",'45' as "Orden",'Colombia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KM' as "Codigo",'46' as "Orden",'Comoras' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CG' as "Codigo",'47' as "Orden",'Congo' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KP' as "Codigo",'48' as "Orden",'Corea del Norte' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KR' as "Codigo",'49' as "Orden",'Corea del Sur' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CI' as "Codigo",'50' as "Orden",'Costa de Marfil' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CR' as "Codigo",'51' as "Orden",'Costa Rica' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'HR' as "Codigo",'52' as "Orden",'Croacia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CU' as "Codigo",'53' as "Orden",'Cuba' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CW' as "Codigo",'54' as "Orden",'Curazao' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'DK' as "Codigo",'55' as "Orden",'Dinamarca' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'DM' as "Codigo",'56' as "Orden",'Dominica' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'EC' as "Codigo",'57' as "Orden",'Ecuador' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'EG' as "Codigo",'58' as "Orden",'Egipto' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SV' as "Codigo",'59' as "Orden",'El Salvador' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AE' as "Codigo",'60' as "Orden",'Emiratos Árabes Unidos' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ER' as "Codigo",'61' as "Orden",'Eritrea' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GB' as "Codigo",'62' as "Orden",'Escocia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SK' as "Codigo",'63' as "Orden",'Eslovaquia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SI' as "Codigo",'64' as "Orden",'Eslovenia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ES' as "Codigo",'65' as "Orden",'España' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'US' as "Codigo",'66' as "Orden",'Estados Unidos' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'EE' as "Codigo",'67' as "Orden",'Estonia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ET' as "Codigo",'68' as "Orden",'Etiopía' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'RU' as "Codigo",'69' as "Orden",'Federación Rusa (Rusia)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PH' as "Codigo",'70' as "Orden",'Filipinas' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'FI' as "Codigo",'71' as "Orden",'Finlandia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'FJ' as "Codigo",'72' as "Orden",'Fiyi' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'FR' as "Codigo",'73' as "Orden",'Francia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GA' as "Codigo",'74' as "Orden",'Gabón' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
--insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GB' as "Codigo",'75' as "Orden",'Gales ' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GM' as "Codigo",'76' as "Orden",'Gambia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GE' as "Codigo",'77' as "Orden",'Georgia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GH' as "Codigo",'78' as "Orden",'Ghana' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GI' as "Codigo",'79' as "Orden",'Gibraltar' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GD' as "Codigo",'80' as "Orden",'Granada' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GR' as "Codigo",'81' as "Orden",'Grecia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GL' as "Codigo",'82' as "Orden",'Groenlandia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GP' as "Codigo",'83' as "Orden",'Guadalupe (Francia)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GU' as "Codigo",'84' as "Orden",'Guam (EE.UU.)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GT' as "Codigo",'85' as "Orden",'Guatemala' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GF' as "Codigo",'86' as "Orden",'Guayana Francesa' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GG' as "Codigo",'87' as "Orden",'Guernsey' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GN' as "Codigo",'88' as "Orden",'Guinea' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GW' as "Codigo",'89' as "Orden",'Guinea Bissau' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GQ' as "Codigo",'90' as "Orden",'Guinea Ecuatorial' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GY' as "Codigo",'91' as "Orden",'Guyana' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'HT' as "Codigo",'92' as "Orden",'Haití' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NL' as "Codigo",'93' as "Orden",'Holanda' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'HN' as "Codigo",'94' as "Orden",'Honduras' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'HK' as "Codigo",'95' as "Orden",'Hong Kong' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'HU' as "Codigo",'96' as "Orden",'Hungría' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IN' as "Codigo",'97' as "Orden",'India' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ID' as "Codigo",'98' as "Orden",'Indonesia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
--insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GB' as "Codigo",'99' as "Orden",'Inglaterra' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IR' as "Codigo",'100' as "Orden",'Irán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IQ' as "Codigo",'101' as "Orden",'Iraq' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IE' as "Codigo",'102' as "Orden",'Irlanda' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BV' as "Codigo",'103' as "Orden",'Isla Bouvet' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IM' as "Codigo",'104' as "Orden",'Isla de Man' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CX' as "Codigo",'105' as "Orden",'Isla de Navidad' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MF' as "Codigo",'106' as "Orden",'Isla de San Martín (parte francesa)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SX' as "Codigo",'107' as "Orden",'Isla de San Martín (parte holandesa)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GS' as "Codigo",'108' as "Orden",'Isla Georgia del Sur e Islas Sandwich del Sur' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NF' as "Codigo",'109' as "Orden",'Isla Norfolk' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PN' as "Codigo",'110' as "Orden",'Isla Pitcairn' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IS' as "Codigo",'111' as "Orden",'Islandia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AX' as "Codigo",'112' as "Orden",'Islas Åland' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KY' as "Codigo",'113' as "Orden",'Islas Caimán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CC' as "Codigo",'114' as "Orden",'Islas Cocos' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CK' as "Codigo",'115' as "Orden",'Islas Cook' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'FK' as "Codigo",'116' as "Orden",'Islas Falkland (Malvinas)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'FO' as "Codigo",'117' as "Orden",'Islas Faroe' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'HM' as "Codigo",'118' as "Orden",'Islas Heard y McDonald' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MP' as "Codigo",'119' as "Orden",'Islas Marianas del Norte' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MH' as "Codigo",'120' as "Orden",'Islas Marshall' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'UM' as "Codigo",'121' as "Orden",'Islas Menores de Estados Unidos de Ultramar' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SB' as "Codigo",'122' as "Orden",'Islas Salomón' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SJ' as "Codigo",'123' as "Orden",'Islas Svalbard y Jan Mayen' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TC' as "Codigo",'124' as "Orden",'Islas Turcas y Caicos' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'VI' as "Codigo",'125' as "Orden",'Islas Vírgenes Americanas' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'VG' as "Codigo",'126' as "Orden",'Islas Vírgenes Británicas' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'WF' as "Codigo",'127' as "Orden",'Islas Wallis y Futuna' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IL' as "Codigo",'128' as "Orden",'Israel' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IT' as "Codigo",'129' as "Orden",'Italia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'JM' as "Codigo",'130' as "Orden",'Jamaica' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'JP' as "Codigo",'131' as "Orden",'Japón' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'JE' as "Codigo",'132' as "Orden",'Jersey' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'JO' as "Codigo",'133' as "Orden",'Jordania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KZ' as "Codigo",'134' as "Orden",'Kazajistán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KE' as "Codigo",'135' as "Orden",'Kenia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KG' as "Codigo",'136' as "Orden",'Kirguistán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KI' as "Codigo",'137' as "Orden",'Kiribati' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KW' as "Codigo",'138' as "Orden",'Kuwait' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LA' as "Codigo",'139' as "Orden",'Laos' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LS' as "Codigo",'140' as "Orden",'Lesoto' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LV' as "Codigo",'141' as "Orden",'Letonia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LB' as "Codigo",'142' as "Orden",'Líbano' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LR' as "Codigo",'143' as "Orden",'Liberia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LY' as "Codigo",'144' as "Orden",'Libia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LI' as "Codigo",'145' as "Orden",'Liechtenstein' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LT' as "Codigo",'146' as "Orden",'Lituania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LU' as "Codigo",'147' as "Orden",'Luxemburgo' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MO' as "Codigo",'148' as "Orden",'Macao' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MK' as "Codigo",'149' as "Orden",'Macedonia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MG' as "Codigo",'150' as "Orden",'Madagascar' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MY' as "Codigo",'151' as "Orden",'Malasia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MW' as "Codigo",'152' as "Orden",'Malawi' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MV' as "Codigo",'153' as "Orden",'Maldivas' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ML' as "Codigo",'154' as "Orden",'Malí' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MT' as "Codigo",'155' as "Orden",'Malta' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MA' as "Codigo",'156' as "Orden",'Marruecos' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MQ' as "Codigo",'157' as "Orden",'Martinica' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MU' as "Codigo",'158' as "Orden",'Mauricio' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MR' as "Codigo",'159' as "Orden",'Mauritania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'YT' as "Codigo",'160' as "Orden",'Mayotte' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MX' as "Codigo",'161' as "Orden",'México' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'FM' as "Codigo",'162' as "Orden",'Micronesia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MD' as "Codigo",'163' as "Orden",'Moldavia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MC' as "Codigo",'164' as "Orden",'Mónaco' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MN' as "Codigo",'165' as "Orden",'Mongolia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ME' as "Codigo",'166' as "Orden",'Montenegro' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MS' as "Codigo",'167' as "Orden",'Montserrat' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MZ' as "Codigo",'168' as "Orden",'Mozambique' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'MM' as "Codigo",'169' as "Orden",'Myanmar (Birmania)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NA' as "Codigo",'170' as "Orden",'Namibia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NR' as "Codigo",'171' as "Orden",'Nauru' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NP' as "Codigo",'172' as "Orden",'Nepal' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NI' as "Codigo",'173' as "Orden",'Nicaragua' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NE' as "Codigo",'174' as "Orden",'Níger' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NG' as "Codigo",'175' as "Orden",'Nigeria' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NU' as "Codigo",'176' as "Orden",'Niue' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NO' as "Codigo",'177' as "Orden",'Noruega' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NC' as "Codigo",'178' as "Orden",'Nueva Caledonia (Francia)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NZ' as "Codigo",'179' as "Orden",'Nueva Zelanda' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'OM' as "Codigo",'180' as "Orden",'Omán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PK' as "Codigo",'181' as "Orden",'Pakistán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PW' as "Codigo",'182' as "Orden",'Palaos' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PS' as "Codigo",'183' as "Orden",'Palestina' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PA' as "Codigo",'184' as "Orden",'Panamá' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PG' as "Codigo",'185' as "Orden",'Papúa Nueva Guinea' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PY' as "Codigo",'186' as "Orden",'Paraguay' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PE' as "Codigo",'187' as "Orden",'Perú' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PF' as "Codigo",'188' as "Orden",'Polinesia (Francia)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PL' as "Codigo",'189' as "Orden",'Polonia (std LE)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PT' as "Codigo",'190' as "Orden",'Portugal' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PR' as "Codigo",'191' as "Orden",'Puerto Rico' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'QA' as "Codigo",'192' as "Orden",'Qatar' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
--insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'GB' as "Codigo",'193' as "Orden",'Reino Unido' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CF' as "Codigo",'194' as "Orden",'República Centro Africana' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CZ' as "Codigo",'195' as "Orden",'República Checa' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CD' as "Codigo",'196' as "Orden",'República Democrática del Congo' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'DO' as "Codigo",'197' as "Orden",'República Dominicana' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'RE' as "Codigo",'198' as "Orden",'Reunión (Francia)' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'RW' as "Codigo",'199' as "Orden",'Ruanda' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'RO' as "Codigo",'200' as "Orden",'Rumania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'EH' as "Codigo",'201' as "Orden",'Sahara Occidental' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'WS' as "Codigo",'202' as "Orden",'Samoa' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'AS' as "Codigo",'203' as "Orden",'Samoa Americana' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'BL' as "Codigo",'204' as "Orden",'San Bartolomé' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'KN' as "Codigo",'205' as "Orden",'San Cristóbal y Nieves' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SM' as "Codigo",'206' as "Orden",'San Marino' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'PM' as "Codigo",'207' as "Orden",'San Pedro y Miguelón' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'VC' as "Codigo",'208' as "Orden",'San Vicente y las Granadinas' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SH' as "Codigo",'209' as "Orden",'Santa Elena, Ascensión y Tristán de Acuña' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LC' as "Codigo",'210' as "Orden",'Santa Lucía' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ST' as "Codigo",'211' as "Orden",'Santo Tomé y Príncipe' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SN' as "Codigo",'212' as "Orden",'Senegal' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'YU' as "Codigo",'213' as "Orden",'Serbia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SC' as "Codigo",'214' as "Orden",'Seychelles' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SL' as "Codigo",'215' as "Orden",'Sierra Leona' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SG' as "Codigo",'216' as "Orden",'Singapur' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SY' as "Codigo",'217' as "Orden",'Siria' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SO' as "Codigo",'218' as "Orden",'Somalia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'LK' as "Codigo",'219' as "Orden",'Sri Lanka' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SZ' as "Codigo",'220' as "Orden",'Suazilandia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ZA' as "Codigo",'221' as "Orden",'Sudáfrica' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SD' as "Codigo",'222' as "Orden",'Sudán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SS' as "Codigo",'223' as "Orden",'Sudán del Sur' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SE' as "Codigo",'224' as "Orden",'Suecia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'CH' as "Codigo",'225' as "Orden",'Suiza' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'SR' as "Codigo",'226' as "Orden",'Surinam' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TH' as "Codigo",'227' as "Orden",'Tailandia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TW' as "Codigo",'228' as "Orden",'Taiwán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TZ' as "Codigo",'229' as "Orden",'Tanzania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TJ' as "Codigo",'230' as "Orden",'Tayikistán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'IO' as "Codigo",'231' as "Orden",'Territorio Británico del Océano Índico' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TF' as "Codigo",'232' as "Orden",'Territorios Australes Franceses' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TL' as "Codigo",'233' as "Orden",'Timor Oriental' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TG' as "Codigo",'234' as "Orden",'Togo' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TK' as "Codigo",'235' as "Orden",'Tokelau' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TO' as "Codigo",'236' as "Orden",'Tonga' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TT' as "Codigo",'237' as "Orden",'Trinidad y Tobago' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TN' as "Codigo",'238' as "Orden",'Túnez' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TM' as "Codigo",'239' as "Orden",'Turkmenistán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TR' as "Codigo",'240' as "Orden",'Turquía' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'TV' as "Codigo",'241' as "Orden",'Tuvalu' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'UA' as "Codigo",'242' as "Orden",'Ucrania' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'UG' as "Codigo",'243' as "Orden",'Uganda' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'UY' as "Codigo",'244' as "Orden",'Uruguay' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'UZ' as "Codigo",'245' as "Orden",'Uzbekistán' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'VU' as "Codigo",'246' as "Orden",'Vanuatu' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'VE' as "Codigo",'247' as "Orden",'Venezuela' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'VN' as "Codigo",'248' as "Orden",'Vietnam' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'YE' as "Codigo",'249' as "Orden",'Yemen' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'DJ' as "Codigo",'250' as "Orden",'Yibouti' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ZR' as "Codigo",'251' as "Orden",'Zaire' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ZM' as "Codigo",'252' as "Orden",'Zambia' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'ZW' as "Codigo",'253' as "Orden",'Zimbabwe' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" Select 'CodigoPais' as "Tipo",'NT' as "Codigo",'254' as "Orden",'Zona neutral' as "Descripcion",'S' as "Activo",'' as "Objetivo",'' as "Producto" FROM DUMMY;


/*==============================================================*/
/* PARAMETROS DE CONFIGURACION                                  */
/*==============================================================*/

delete from "BSCL.Normativa.BD::NOR.Configuracion"
where "IdEmpresa"=1 and "CodigoParametro"='RaizArchivos';

insert into "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
values(1, 'RaizArchivos', 'Ruta raiz de los archivos', '/mnt/optimus/des/empresa1/nor');

insert into "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
values(1, 'CodigoEmpresaDJInternet', 'Codigo empresa para DJ por Internet', '91');

insert into "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
values(1, 'CodigoEmpresaDJMagnetico', 'Codigo empresa para DJ por medio magnetico', '80');



/*==============================================================*/
/* CONFIGURACION DE MANTENEDOR                                  */
/*==============================================================*/
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_TabDatosDeclaracion',     'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_TabDatosDeclarante',      'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_TabAuditoriaCarga',       'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_TabAuditoriaCuadratura',  'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_TabAuditoriaGeneracion',  'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_TabAuditoriaValidacion',  'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_BtnCrear',                'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_BtnCargar',               'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_BtnCuadrar',              'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_BtnGenerarCertificado',   'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_BtnGenerarDeclaracion',   'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'DECLACORR', 'FormDetailView_BtnExportarExcel',        'S' FROM DUMMY;

-- CI 1720
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_TabDatosDeclaracion',     'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_TabDatosDeclarante',      'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_TabAuditoriaCarga',       'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_TabAuditoriaCuadratura',  'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_TabAuditoriaGeneracion',  'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_TabAuditoriaValidacion',  'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_BtnCrear',                'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_BtnCargar',               'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_BtnCuadrar',              'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_BtnGenerarCertificado',   'N' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_BtnGenerarDeclaracion',   'S' FROM DUMMY;
insert into "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" SELECT 'FormDetailView', 'CI1720', 'FormDetailView_BtnExportarExcel',        'S' FROM DUMMY;

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
