-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 06-06-2016
--

--####################################################################################


DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::GenerarMovimientoCustodia_v1";

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearMovimientoComprobante";

INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
("IdConceptoFacturacion", "IdConcepto", "NombreEntidad", "IdTipoComprobante", "IdTipoProducto", "IdTipoOperacion", "CodigoPreferencia")
VALUES(46, 'FOANPR', '', 'FAO', 'INAC', 'ANTPRT', 'TOTAL');