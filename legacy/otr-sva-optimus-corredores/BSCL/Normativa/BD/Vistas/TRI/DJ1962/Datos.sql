SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1962.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1962.Datos" (
    "ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
   	,"IdOperacion"
    ,"IdEmisor"	
    ,"Fondo"
    ,"IdCliente"
    ,"IdPortafolio"				
	,"RutAdministradora"						
	,"RutFondoB"	
    ,"RunFondo"
    ,"TipoFondo"
    ,"TributaSa"
    ,"DetBenNetoPerPOS_C6"
    ,"DetBenNetoPerNEG_C7"
    ,"DetBenNetoUtilPer_C8"
    ,"DetBenNetoIntPer_C9"
    ,"DetBenNetoDivPer_C10"
    ,"DetBenNetoGanCapPer_C11"
    ,"DetBenNetoPerGastDev_C12"
    ,"Numero"
    ,"RutFondoC"
    ,"RutCliente"
    ,"NombreParticipe"
    ,"TaxId"
    ,"CodigoPais"
    ,"MonedaReajuste"
    ,"MontoAporteNominal"
    ,"MontoNominalAportesActualizado"
    ,"CantidadCuotasParticipe"
    ,"ParticipeCumpleRequisitos"
    ,"CuotasCumplenRequisitos"
    ,"ValorCuotaPesos"
    ,"PorcentajeParticipacion31Dic"
    ,"TipoContribuyente"
    ,"FechaRespaldo"
    ,"FolioCartera"
	,"FechaOperacion"
    ,"IdCartera"
) AS
SELECT
    MAX("ID")
    ,"IdDocumento"
    ,MAX("VboUsuario")
    ,MAX("Observaciones") 
    ,MAX("FuenteDatos")
    ,MAX("IdOperacion")
    ,MAX("IdEmisor") 
    ,"Fondo"		
    ,MAX("IdCliente")
    ,MAX("IdPortafolio")					
	,MAX("RutAdministradora")						
	,MAX("RutFondoB")
	,MAX("RunFondo")
    ,MAX("TipoFondo")
    ,MAX("TributaSa")
    ,MAX("DetBenNetoPerPOS_C6")
    ,MAX("DetBenNetoPerNEG_C7")
    ,MAX("DetBenNetoUtilPer_C8")
    ,MAX("DetBenNetoIntPer_C9")
    ,MAX("DetBenNetoDivPer_C10")
    ,MAX("DetBenNetoGanCapPer_C11")
    ,MAX("DetBenNetoPerGastDev_C12")
    ,MAX("Numero")
    ,"RutFondoC"
    ,"RutCliente"
    ,MAX("NombreParticipe")
    ,MAX("TaxId")
    ,MAX("CodigoPais")
    ,"MonedaReajuste"
    ,SUM("MontoAporteNominal")
    ,SUM("MontoNominalAportesActualizado")
    ,SUM("CantidadCuotasParticipe")
    ,MAX("ParticipeCumpleRequisitos")
    ,MAX("CuotasCumplenRequisitos")
    ,MAX("ValorCuotaPesos")
    ,MAX("PorcentajeParticipacion31Dic")
    ,MAX("TipoContribuyente")
    ,MAX("FechaRespaldo")
    ,MAX("FolioCartera")
	,MAX("FechaOperacion")
    ,MAX("IdCartera")

    FROM "BSCL.Normativa.BD::TRI.DatosDJ1962" a

    GROUP BY "IdDocumento","Fondo","RutFondoC","RutCliente","MonedaReajuste"

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1962.Datos" TO bcs_desarrollador WITH GRANT OPTION;
