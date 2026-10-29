SET SCHEMA "BSCL";

--TRIGGER
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_DropCopyRvMarcaNegociacion";

CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControl_DropCopyRvMarcaNegociacion"              
AFTER INSERT ON "BSCL.Orden.BD::DPC.TBEXECUTIONREPORT"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('DROPCOPYRV_MARCA_NEGOCIACION',:mensaje."ORDERID",:mensaje."EXECID",CURRENT_TIMESTAMP,'N');
END;

--TIPO PROGRAMA
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
SELECT 'DropCopyRvMarcaNegociacion' as "IdTipoPrograma",'DropCopyRvMarcaNegociacion' AS "Descripcion",1 AS "MaximoIntentos",'Ordenes' AS "Modulo",
'cl.bcs.ordenes.jobs.DropCopyRvMarcaNegociacion' AS"NombreClasePrograma",'JAVA' AS "TipoClasePrograma",'D' as "Nodo",'N' AS "EsPauta" FROM DUMMY

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
	p_IdTipoTarea		=> 'DropCopyRvMarcaNegociacion',
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
