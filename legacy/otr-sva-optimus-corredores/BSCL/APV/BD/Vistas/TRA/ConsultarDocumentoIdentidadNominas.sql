SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarDocumentoIdentidadNominas"; 
GO

CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarDocumentoIdentidadNominas" AS 
		
	SELECT 
		 DISTINCT Traspaso."DocumentoIdentidadDefecto"
		,Traspaso."NombreCompleto"
		,Traspaso."DocumentoEntidad"
		,Traspaso."NombreEntidad"
		,Traspaso."IdEmpresa"
		FROM "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasosAPV" AS Traspaso
		INNER JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso 
			 ON OrdenTraspaso."IdTraspaso" =Traspaso."IdTraspaso"
		INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporte 
			 ON SolicitudAporte."IdSolicitudAporteRescate" = OrdenTraspaso."IdSolicitudAporteRescate"
			 AND  SolicitudAporte."Estado"= 'CRT'
		WHERE Traspaso."IdEstado"='COM'
		AND Traspaso."TipoMovimiento"='EGRESO';
GO	 	
GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarDocumentoIdentidadNominas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO