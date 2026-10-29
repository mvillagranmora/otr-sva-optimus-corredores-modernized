SET SCHEMA "BSCL";
GO
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_RF";
GO
CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_RF"              
AFTER INSERT ON "BSCL.Orden.BD::MRF.PALOS_RF"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('MARKET_DATA_PALO_RF',:mensaje."timeTicker",:mensaje."tradeID",CURRENT_UTCTIMESTAMP,'N');
	--VALUES ('RESPUESTA_DMA_AGENTE',:mensaje."NRO_ENTRADA",:mensaje."FECHA",CURRENT_TIMESTAMP,'N');
END;
GO
--TIPO PROGRAMA
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
SELECT 'ControlMercadoMarketDataPalosRF' as "IdTipoPrograma",'ControlMercadoMarketDataPalosRF' AS "Descripcion",1 AS "MaximoIntentos",'Ordenes' AS "Modulo",
'cl.bcs.ordenes.jobs.ControlMercadoMarketDataPalosRF' AS"NombreClasePrograma",'JAVA' AS "TipoClasePrograma",'D' as "Nodo",'N' AS "EsPauta" FROM DUMMY

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.Agenda"
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"

--AGENDA
DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);

INSERT INTO #TT
select 'IdEmpresa' AS "NombreParametro", '1' as "ValorParametro" FROM DUMMY;
INSERT INTO #TT
select 'IdUsuario' AS "NombreParametro", '0' as "ValorParametro" FROM DUMMY;

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'ControlMercadoMarketDataPalosRF',
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
	
	
