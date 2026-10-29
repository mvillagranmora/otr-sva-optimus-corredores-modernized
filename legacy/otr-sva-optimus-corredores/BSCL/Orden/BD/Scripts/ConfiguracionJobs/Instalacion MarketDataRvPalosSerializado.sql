SET SCHEMA "BSCL";

DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_SERIALIZADOS";

CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_SERIALIZADOS"              
AFTER INSERT ON "BSCL.Orden.BD::MRV.PALOS_SERIALIZADOS"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('MARKET_DATA_PALO_SERIALIZADO',:mensaje."timesTamp",:mensaje."folio",CURRENT_TIMESTAMP,'N');
	--VALUES ('RESPUESTA_DMA_AGENTE',:mensaje."NRO_ENTRADA",:mensaje."FECHA",CURRENT_TIMESTAMP,'N');
END;


DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);

INSERT INTO #TT
select 'IdEmpresa' AS "NombreParametro", '1' as "ValorParametro" FROM DUMMY;
INSERT INTO #TT
select 'IdUsuario' AS "NombreParametro", '0' as "ValorParametro" FROM DUMMY;

--SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.Agenda";
--SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.ParametrosAgenda" where "IdAgenda" = 10016

--update "BSCL"."BSCL.Plataforma.BD::BAT.ParametrosAgenda" set "Valor" = 0 where "IdAgenda" = 10016 and "NombreParametro" = 'IdUsuario'

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'ControlMercado',
	p_EsDemonio			=> 'S',  
	p_DiaDeMes			=> '**',
	p_DiaDeSemana		=> '*',
	p_Mes				=> '**',
	p_Hora				=> '**',
	p_Minuto			=> '01',
	p_SoloDiasHabiales	=> 'N',
	p_SoloPrincipioMes	=> 'N',
	p_SoloFinDeMes		=> 'N',
	p_habilitado		=> 'S',
	p_TablaParametros	=> #TT,
	
	p_IdAgenda					=> ?,
	p_IdPrograma				=> ?,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=> ?,
	p_out_ErroresContador 		=> ?,	
	p_out_ErroresDetalle		=> ?);
	
	commit;
