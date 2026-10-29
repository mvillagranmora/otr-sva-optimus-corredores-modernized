SET SCHEMA "BSCL";
GO
--TRIGGER
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_IF";
GO
CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_PALOS_IF"              
AFTER INSERT ON "BSCL.Orden.BD::MRF.PALOS_IF"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('MARKET_DATA_PALO_IF',:mensaje."TIMETICKER",:mensaje."TRADEID",CURRENT_UTCTIMESTAMP,'N');
END;
GO
--TIPO PROGRAMA
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
SELECT 'ControlMercadoMarketDataPalosIF' as "IdTipoPrograma",'ControlMercadoMarketDataPalosIF' AS "Descripcion",1 AS "MaximoIntentos",'Ordenes' AS "Modulo",
'cl.bcs.ordenes.jobs.ControlMercadoMarketDataPalosIF' AS"NombreClasePrograma",'JAVA' AS "TipoClasePrograma",'D' as "Nodo",'N' AS "EsPauta" FROM DUMMY

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
	p_IdTipoTarea		=> 'ControlMercadoMarketDataPalosIF',
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
	
	
