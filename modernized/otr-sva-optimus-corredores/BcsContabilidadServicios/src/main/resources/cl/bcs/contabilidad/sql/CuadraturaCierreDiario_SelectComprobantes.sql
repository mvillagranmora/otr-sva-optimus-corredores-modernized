SELECT CTB_itzPreComprobante."IdPreComprobante" 									AS "IdPreComprobante",
   IFNULL(CTB_itzEmpresa."Nombre", 'S/N') 										AS "NombreEmpresa",
   CTB_itzPreComprobante."Ejercicio"											AS "Ejercicio",
   CAST(CAST(CTB_itzPreComprobante."FechaContable" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaContable",
   CAST(CAST(CTB_itzPreComprobante."FechaProceso" AS TIMESTAMP) AS CHAR(10)) 	AS "FechaProceso",
   CTB_itzPreComprobante."HoraProceso" 											AS "HoraProceso",
   CTB_itzPreComprobante."FolioContabilidad" 									AS "FolioContabilidad",
   (SELECT COUNT(1) 
      FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
 WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" 
   AND CTB_itzPreComprobanteAsiento."Estado"  IN ('V','C','A')) 			AS "CantidadAsientos",
   CTB_itzPreComprobante."Estado" 												AS "Estado",
   EstadoContabilidad."NombreEstado"											AS "NombreEstado",
   CTB_itzPreComprobante."Glosa"												AS "Glosa",
   CTB_itzPreComprobante."IdReversa"											AS "IdReversa",
   CTB_itzPreComprobante."Tipo" 												AS "Tipo",
   CTB_itzPreComprobante."CodigoUsuario" 										AS "CodigoUsuario",
   CTB_itzPreComprobante."IdTipoContabilidad" 									AS "IdTipoContabilidad",
   CTB_itzContabilidadTipo."Nombre" || ' - ' || TRIM(CAST("IdPreComprobante" AS NVARCHAR(20))) AS "NombreTipoContabilidad", 
   CTB_itzPreComprobante."IdEmpresa" 											AS "IdEmpresa",
   (SELECT SUM(CAST("MontoDebe" AS DECIMAL)) 
  FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento 
 WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" 
   AND CTB_itzPreComprobanteAsiento."Estado" IN ('V','C','A')) 					AS "Debe",
   (SELECT SUM(CAST("MontoHaber" AS DECIMAL)) 
  FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
 WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = CTB_itzPreComprobante."IdPreComprobante" 
   AND CTB_itzPreComprobanteAsiento."Estado" IN ('V','C','A')) 					AS "Haber"
  FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante 
LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo ON 
CTB_itzContabilidadTipo."IdTipoContabilidad" = CTB_itzPreComprobante."IdTipoContabilidad" 
LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.Empresa" AS CTB_itzEmpresa ON 
CTB_itzEmpresa."IdEmpresa" = CTB_itzPreComprobante."IdEmpresa" 
LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  AS EstadoContabilidad ON 
EstadoContabilidad."IdEstadoContabilidad" = CTB_itzPreComprobante."Estado"
 WHERE (CTB_itzPreComprobante."IdEmpresa" = :IdEmpresa) 
   AND (CTB_itzPreComprobante."FechaContable" = :FechaCentralizacion) 