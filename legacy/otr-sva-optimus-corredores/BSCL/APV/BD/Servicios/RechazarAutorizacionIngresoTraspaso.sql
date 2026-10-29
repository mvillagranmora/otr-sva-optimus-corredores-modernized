SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.APV.BD.Servicios::RechazarAutorizacionIngresoTraspaso";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para rechazar Ingreso o Egreso TraspasoAPV.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: PRONETIT
--  Fecha Creacion: 19-04-2018
--  
"BSCL.APV.BD.Servicios::RechazarAutorizacionIngresoTraspaso" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca		  
	IN 	p_Traspasos						TABLE ("IdTraspaso"	BIGINT)	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_FechaOperacionSecondate SECONDDATE;
	DECLARE v_countContratos BIGINT;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaOperacionSecondate);
	    /*Actualizo Estado Traspaso*/
	v_Traspasos = SELECT row_number() over (order by "IdTraspaso") AS "IdInterno","IdTraspaso" FROM :p_Traspasos;
	CALL "BSCL.APV.BD.Servicios::CambiarEstadoTraspasoAPV" (
		p_Traspaso 		=> :v_Traspasos,
		p_NuevoEstado	=> 'RZD'
	);
  	
  	/*Actualizo estado Contrato*/
  	v_Contratos = SELECT row_number() over (order by Contrato."FolioContratoSAAPV") AS "IdInterno",Contrato."FolioContratoSAAPV" 
  				  FROM "BSCL.APV.BD::TRA.Traspaso" AS Traspaso 
  				  INNER JOIN :p_Traspasos AS TRA
  				  		  ON TRA."IdTraspaso" = Traspaso."IdTraspaso"
  				  INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
		 	    		  ON Contrato."IdContratoSAAPV" = Traspaso."IdContratoSAAPV"
		 	   			 AND Contrato."IdCliente" = Traspaso."IdCliente"
		 	   			 AND Contrato."CodigoTipoSolicitud" = 'TRA';
		 	   			 
	CALL "BSCL.APV.BD.Servicios::CambiarEstadoContratoSAAAPV" (
 		p_Contratos  			=> :v_Contratos,
    	p_NuevoEstado  		    => '',
    	p_NuevoEstadoSolicitud 	=> 'RZD'
	);

END;