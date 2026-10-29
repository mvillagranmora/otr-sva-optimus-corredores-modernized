SET SCHEMA "BSCL";

SELECT * FROM "BSCL"."BSCL.Orden.BD::MRF.PALOS_RF"
SELECT * FROM "BSCL"."BSCL.Orden.BD::MRF.PALOS_IF"
SELECT TOP 100 * FROM "BSCL.Orden.BD::ORD.ControlMercado" WHERE "TipoMensaje" = 'MARKET_DATA_PALO_RF' ORDER BY "FechaIngresoRegistro" desc;
--SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.CorredoraCodigos"


DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_RF";

CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_RF"              
AFTER INSERT ON "BSCL.Orden.BD::MRF.PALOS_RF"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('MARKET_DATA_PALO_RF',:mensaje."timeTicker",:mensaje."tradeID",CURRENT_TIMESTAMP,'N');
	--VALUES ('RESPUESTA_DMA_AGENTE',:mensaje."NRO_ENTRADA",:mensaje."FECHA",CURRENT_TIMESTAMP,'N');
END;

DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);


SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.Agenda"
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.Programa"
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma" = 'ControlMercado'
DELETE FROM "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma" = 'ControlMercado'

INSERT INTO #TT
select 'IdEmpresa' AS "NombreParametro", '1' as "ValorParametro";
INSERT INTO #TT
select 'IdUsuario' AS "NombreParametro", '0' as "ValorParametro";

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
