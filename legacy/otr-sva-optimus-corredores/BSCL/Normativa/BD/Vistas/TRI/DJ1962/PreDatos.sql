SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1962.PreDatos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1962.PreDatos" (
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
    ,"TipoFondoDescripcion"
    ,"TributaSa"
    ,"TributaSaDescripcion"
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
    ,"CodigoPaisDescripcion"
    ,"MonedaReajuste"
    ,"MonedaReajusteDescripcion"
    ,"MontoAporteNominal"
    ,"MontoNominalAportesActualizado"
    ,"CantidadCuotasParticipe"
    ,"ParticipeCumpleRequisitos"
    ,"ParticipeCumpleRequisitosDescripcion"
    ,"CuotasCumplenRequisitos"
    ,"CuotasCumplenRequisitosDescripcion"
    ,"ValorCuotaPesos"
    ,"PorcentajeParticipacion31Dic"
    ,"TipoContribuyente"
    ,"TipoContribuyenteDescripcion"
    ,"FechaRespaldo"
    ,"FolioCartera"
	,"FechaOperacion"
    ,"IdCartera"
) AS
SELECT
    a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones"
    
    ,a."FuenteDatos"
    
   	,a."IdOperacion"

    ,Case 
	WHEN (a."IdEmisor" IS NULL OR a."IdEmisor" = '') AND a."FuenteDatos" = 'EXTERNA'  THEN COALESCE(emi."IdEmisor",'') 
	Else a."IdEmisor"
	End As "IdEmisor" 

    ,a."Fondo"		
    ,a."IdCliente"
    ,a."IdPortafolio"						
	,a."RutAdministradora"
									
	,Case 
	    WHEN (a."RutFondoB" IS NULL OR a."RutFondoB" = '') AND a."FuenteDatos" = 'EXTERNA'  THEN  
	    	CASE WHEN a."TipoFondo"='F' THEN COALESCE(fondo."CodigoSVS",'') ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END
	    ELSE a."RutFondoB" 
	 End As "RutFondoB"
	,Case 
	    WHEN (a."RunFondo" IS NULL OR a."RunFondo" = '') AND a."FuenteDatos" = 'EXTERNA'  THEN  
	    	CASE WHEN a."TipoFondo"='F' THEN COALESCE(fondo."CodigoSVS",'') ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END
	    ELSE a."RunFondo" 
	 End As "RunFondo"
    ,a."TipoFondo"
    ,g."Descripcion" AS "TipoFondoDescripcion"
    ,a."TributaSa"
    ,t."Descripcion" AS "TributaSaDescripcion"
    ,a."DetBenNetoPerPOS_C6"
    ,a."DetBenNetoPerNEG_C7"
    ,COALESCE(a."DetBenNetoUtilPer_C8",0)
    ,COALESCE(a."DetBenNetoIntPer_C9",0)
    ,COALESCE(a."DetBenNetoDivPer_C10",0)
    ,COALESCE(a."DetBenNetoGanCapPer_C11",0)
    ,COALESCE(a."DetBenNetoPerGastDev_C12",0)
    ,a."Numero"
    ,Case 
	    WHEN (a."RutFondoC" IS NULL OR a."RutFondoC" = '') AND a."FuenteDatos" = 'EXTERNA'  THEN  
	    	CASE WHEN a."TipoFondo"='F' THEN COALESCE(fondo."CodigoSVS",'') ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END
	    ELSE a."RutFondoC" 
    END AS "RutFondoC"
    ,a."RutCliente"
    ,a."NombreParticipe"
    ,a."TaxId"
    ,a."CodigoPais"
    ,h."Descripcion" AS "CodigoPaisDescripcion"
    ,a."MonedaReajuste"
    ,j."Descripcion" AS "MonedaReajusteDescripcion"
    ,a."MontoAporteNominal"
    ,a."MontoNominalAportesActualizado"
    ,a."CantidadCuotasParticipe"
    ,a."ParticipeCumpleRequisitos"
    ,k."Descripcion" AS "ParticipeCumpleRequisitosDescripcion"
    ,a."CuotasCumplenRequisitos"
    ,l."Descripcion" AS "CuotasCumplenRequisitosDescripcion"
    ,a."ValorCuotaPesos"
    ,a."PorcentajeParticipacion31Dic"
    ,a."TipoContribuyente"
    ,m."Descripcion" as "TipoContribuyenteDescripcion"
    ,a."FechaRespaldo"
    ,a."FolioCartera"
	,a."FechaOperacion"
    ,a."IdCartera"

FROM "BSCL.Normativa.BD::TRI.DatosDJ1962" a
INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" d ON (d."IdDocumento" = a."IdDocumento")
LEFT  JOIN "BSCL.Normativa.BD::TRI.Fondos" f ON (a."Fondo" = f."IdFondo" AND f."Esquema" = 'DJ1962' AND f."Version" = d."Version" AND f."IdEstado" = 'ACT')
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" t ON t."Tipo"='TributaSa1962' and a."TributaSa"=t."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" g    on g."Tipo"='TipoFondo1962' and a."TipoFondo"=g."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" h    on h."Tipo"='CodigoPais1962' and a."CodigoPais"=h."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" j    on j."Tipo"='TipoReajuste1962' and a."MonedaReajuste"=j."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" k    on k."Tipo"='ParticipeCumpleRequisitos1962' and a."ParticipeCumpleRequisitos"=k."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" l    on l."Tipo"='CuotasCumplenRequisitos1962' and a."CuotasCumplenRequisitos"=l."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" m    on m."Tipo"='TipoContribuyente1962' and a."TipoContribuyente"=m."Codigo"
LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS fondo ON trim(fondo."IdSerieFondo") = trim(a."Fondo")
LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."Nemotecnico") = trim(a."Fondo") AND ins."IdClasificacionTipoInstrumento" = 'RV'
LEFT JOIN  "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
LEFT  JOIN "BSCL.Plataforma.BD::MER.Emisor" emi ON (COALESCE(ins."IdEmisor", fondo."IdEmisor") = emi."IdEmisor")
LEFT JOIN (
    SELECT
        "CodigoValidacion","IdDocumento","Fondo","RutFondoC","RutCliente","MonedaReajuste"
    FROM (
        SELECT
            R."CodigoValidacion",
            DT."IdDocumento",
            DT."Fondo",
            DT."RutFondoC",
            DT."RutCliente",
            DT."MonedaReajuste",
            ROW_NUMBER() OVER (
                PARTITION BY
                    DT."IdDocumento",
                    DT."Fondo",
                    DT."RutFondoC",
                    DT."RutCliente",
                    DT."MonedaReajuste"
                ORDER BY R."ID" ASC
            ) AS rn
        FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" R
        INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1962" DT
            ON DT."IdDocumento" = R."IdDocumento"
        INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" DV
            ON DV."IdResumenValidacion" = R."ID"
            AND DV."IdDetalleDocumento" = DT."ID"
    ) t
    WHERE rn = 1
) VAL
ON  a."IdDocumento" = VAL."IdDocumento"AND a."Fondo" = VAL."Fondo" AND a."RutFondoC" = VAL."RutFondoC" AND a."RutCliente" = VAL."RutCliente" AND a."MonedaReajuste" = VAL."MonedaReajuste"


WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1962.PreDatos" TO bcs_desarrollador WITH GRANT OPTION;
