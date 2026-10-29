--GCMBIPAPAS-1 Filtro de clientes por Perfiles
--ConsultarSolicitudAgf_SelectVisorAgf

SELECT 
 InterfazAgf."IdInterfazAgf"
 ,InterfazAgf."DocumentoIdentidad"
 ,InterfazAgf."CodigoPortafolio"
 ,InterfazAgf."TipoRegistro"
 ,InterfazAgf."NombreCompleto"
 --,InterfazAgf."FechaLiquidacion"
 ,TO_NVARCHAR(InterfazAgf."FechaLiquidacion",'YYYY-MM-DD') AS "FechaLiquidacion"
 ,TO_NVARCHAR(InterfazAgf."FechaAceptacion",'YYYY-MM-DD') AS "FechaAceptacion"
 ,TO_NVARCHAR(InterfazAgf."FechaIngreso",'YYYY-MM-DD') AS "FechaIngreso"
 ,TO_NVARCHAR(InterfazAgf."FechaIngresoRegistro",'YYYY-MM-DD HH24:MI:SS') AS "FechaIngresoRegistro"
 ,InterfazAgf."TipoSolicitud"
 ,InterfazAgf."IdEstadoSolicitud"
 ,InterfazAgf."IdSerieFondo"
 ,InterfazAgf."IdUsuario"
 ,InterfazAgf."NumeroCuotas"
 ,InterfazAgf."ValorCuota"
 ,InterfazAgf."Monto"
 ,InterfazAgf."CalculoPorMontoCuota" 
 ,InterfazAgf."IdMoneda"
 ,InterfazAgf."PropositoSerie"
 ,InterfazAgf."IdSerieFondoDestino"
 ,InterfazAgf."IdEstadoSerieFondo"
 ,InterfazAgf."IndicadorMLT"
 ,InterfazAgf."IdEstado"
 ,Estado."Nombre" AS "NombreEstado"
 ,TO_NVARCHAR(InterfazAgf."IdSolicitudAporteRescate") AS "IdSolicitudAporteRescate"
 ,TO_NVARCHAR(InterfazAgf."IdSolicitudAporteRescateRelacionado") AS "IdSolicitudAporteRescateRelacionado" 
 ,InterfazAgf."Observacion"
 ,TO_NVARCHAR(InterfazAgf."IdEmpresa") AS "IdEmpresa" 
FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS InterfazAgf
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS Estado ON (InterfazAgf."IdEstado" = Estado."IdEstado") 

WHERE (TO_DATE(InterfazAgf."FechaIngreso") BETWEEN TO_DATE(:p_FechaIngresoDesde) AND TO_DATE(:p_FechaIngresoHasta))
AND (InterfazAgf."IdSerieFondo" IN (:p_Instrumento) OR 1 = :p_FiltroInstrumento)
AND (InterfazAgf."IdEstado" IN (:p_EstadoSolicitud) OR 1 = :p_FiltroEstado)   
AND (InterfazAgf."TipoRegistro" IN (:p_TipoRegistro) OR 1 = :p_FiltroTipoRegistro)
AND InterfazAgf."IdCliente" IN (:p_listaClientes)  