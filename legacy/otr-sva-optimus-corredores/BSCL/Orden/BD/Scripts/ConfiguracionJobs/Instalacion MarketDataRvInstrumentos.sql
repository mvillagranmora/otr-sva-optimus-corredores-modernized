SET SCHEMA "BSCL";

/*SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" where "Fecha" = '2017-03-27';
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" where "Fecha" = '2017-03-28';
SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ControlMercado" WHERE "TipoMensaje" = 'ACTUALIZACION_INSTRUMENTOS_RV' and "Procesado" = 'N';
delete FROM "BSCL"."BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" where "Fecha" = '2017-03-27';
delete FROM "BSCL"."BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" where "Fecha" = '2017-03-28';
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.Agenda"
delete FROM "BSCL"."BSCL.Plataforma.BD::BAT.Agenda" where "IdAgenda" = 10052
commit;*/

--TRIGGER
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlINSTRUMENTOS_RV";

CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlINSTRUMENTOS_RV"              
AFTER UPDATE ON "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('ACTUALIZACION_INSTRUMENTOS_RV',CURRENT_TIMESTAMP,:mensaje."INSTRUMENTS",CURRENT_TIMESTAMP,'N');
END;

--
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlINSTRUMENTOS_RV_PERU";

CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlINSTRUMENTOS_RV_PERU"              
AFTER INSERT ON "BSCL.Orden.BD::MRV.INSTRUMENTOS_RV"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('ACTUALIZACION_INSTRUMENTOS_RV_PERU',CURRENT_TIMESTAMP,:mensaje."INSTRUMENTS",CURRENT_TIMESTAMP,'N');
END;

--TIPO PROGRAMA
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
SELECT 'MarketDataRvInstrumentos' as "IdTipoPrograma",'MarketDataRvInstrumentos' AS "Descripcion",1 AS "MaximoIntentos",'Ordenes' AS "Modulo",
'cl.bcs.ordenes.jobs.MarketDataRvInstrumentos' AS"NombreClasePrograma",'JAVA' AS "TipoClasePrograma",'D' as "Nodo",'N' AS "EsPauta" FROM DUMMY

--SELECT * FROM "BSCL.Plataforma.BD::BAT.TipoPrograma"
--SELECT * FROM "BSCL.Plataforma.BD::BAT.Agenda"

--AGENDA
DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'MarketDataRvInstrumentos',
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
