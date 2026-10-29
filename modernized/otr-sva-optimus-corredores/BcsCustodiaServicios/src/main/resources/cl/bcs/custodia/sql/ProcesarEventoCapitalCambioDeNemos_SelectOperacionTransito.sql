WITH CTE AS (
SELECT  
    IFNULL(SO."IdSocioDeNegocio",0)                                         as "IdSocioNegocio",                            
    CASE                                                                                                                    
        WHEN SO."IdCliente" > 0         THEN 'CLI'                                                                          
        WHEN SO."IdProveedor" > 0       THEN 'PRO'                                                                          
        WHEN SO."IdEmpleado" > 0        THEN 'EMP'                                                                          
        WHEN SO."IdContraparte" > 0     THEN 'CNT'                                                                          
    END                                                                     as "TipoSocioNegocio",                          
    CA."IdPortafolio"                                                       as "IdPortafolio",                              
    'R'                                                                     as "IdTipoDocumentoIdentidad",                  
    IFNULL(CL."DocumentoIdentidad",'')                                      as "DocumentoIdentidad",                        
    IFNULL(CL."NombreCompleto",'')                                          as "NombreCliente",                             
    CA."IdClasificacionTipoInstrumento"                                     as "IdClasificacionTipoInstrumento",            
    CA."IdTipoInstrumento"                                                  as "IdTipoInstrumento",                         
    CA."IdInstrumento"                                                      as "IdInstrumento",                             
    CA."Nemotecnico"                                                        as "Nemo",                                      
    'TRA'                                                                   as "TipoCartera",                               
    CA."IdBoveda"                                                           as "IdBoveda",
    CA."IdBolsa"                                                            as "IdBolsa",
    ''                                                                      as "IdConcepto",
    CA."IdCartera"                                                          AS "IdCartera",
    CA."FolioCartera"                                                       AS "FolioCartera",              
    'N'                                                                     as "AfectoaClearing",
    CA."INGCantidad"                                            			AS "CantidadDerecho"   
FROM "BSCL.Custodia.BD::CUS.Cartera"                                                AS CA
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion"                          AS AG
    ON CA."EGRIdOperacion" = AG."IdTransaccion"
INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion"                         AS TR
    ON AG."IdTransaccion" = TR."IdTransaccion"

LEFT JOIN   "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"                 AS CL
    ON (CA."IdCliente" = CL."IdCliente")
LEFT JOIN   "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"          AS SO
    ON (CL."IdCliente" = SO."IdCliente")
        

WHERE   CA."IdEmpresa"      = :p_IdEmpresa
AND     TR."IdEmpresa"      = CA."IdEmpresa"
AND     CA."Nemotecnico"  IN (:p_Nemo)
AND     CA."IdEstado"       != 'ANU'
AND     AG."IndicadorOrdenDirecta" = ''
AND (
		:p_IdInstrumento IS NULL 
		OR CA."IdInstrumento" IN (:p_IdInstrumento)
    )

GROUP BY    
    CA."IdBolsa",
    IFNULL(SO."IdSocioDeNegocio",0),
    CASE 
        WHEN SO."IdCliente"     > 0         THEN 'CLI'
        WHEN SO."IdProveedor"   > 0         THEN 'PRO'
        WHEN SO."IdEmpleado"    > 0         THEN 'EMP'
        WHEN SO."IdContraparte" > 0         THEN 'CNT'
    END         ,
    CA."IdPortafolio",
    IFNULL(CL."DocumentoIdentidad",''),
    CL."NombreCompleto",
    CA."IdClasificacionTipoInstrumento",
    CA."IdTipoInstrumento",
    CA."IdInstrumento",
    CA."Nemotecnico",
    AG."AccionTipoOperacion",
    CA."IdBoveda",
    CA."IdCartera",
    CA."FolioCartera",
    CA."INGCantidad")
    

   SELECT 
		CA."IdSocioNegocio",
		CA."TipoSocioNegocio",
		CA."IdPortafolio",
		CA."IdTipoDocumentoIdentidad",
		CA."DocumentoIdentidad",
		CA."NombreCliente",
		CA."IdClasificacionTipoInstrumento",
		CA."IdTipoInstrumento",
		CA."IdInstrumento",
		CA."Nemo",
		CA."TipoCartera",
		CA."IdBoveda",
		CA."IdBolsa",
		CA."IdConcepto",
		CA."IdCartera",
		CA."FolioCartera",
		CA."AfectoaClearing",
		CA."CantidadDerecho",      
	    COALESCE(EJ."IdEmpleadoEjecutivoPortafolio",0)     as "IdEmpleadoEjecutivoPortafolio",
	    COALESCE(EJ."CodigoEmpleado",'')                   as "CodigoUsuarioAgente",                 
	    COALESCE(EJ."NombreCompletoEjecutivo",'')          as "NombreAgente",                        
	    COALESCE(EJ."CodigoSucursal",'')                   as "CodigoSucursal"   
	FROM 
		CTE																				AS CA									
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"       AS EJ
	    ON (CA."IdPortafolio" = EJ."IdPortafolio");
		
   