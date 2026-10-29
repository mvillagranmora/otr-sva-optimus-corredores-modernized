SELECT
"IdPreComprobanteAsiento"
,"IdEmpresa"
,"IdPreComprobante"  
,"IdCuentaContable"  
,"Ejercicio"
,"Linea"
,CASE   
	WHEN "MontoDebe" > 0 THEN 0   
	ELSE "MontoHaber"  
 END AS "MontoDebe"  --+ Se voltean los Valores del Haber hacia el Debe  
,CASE   
	WHEN "MontoHaber" > 0 THEN 0  
	ELSE "MontoDebe"  
 END AS "MontoHaber" --+ Se voltean los Valores del Debe hacia al Haber   
,"FechaContable"
,"Glosa"
,"Estado"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento"
WHERE "IdPreComprobante" IN (:IdPreComprobanteAnt)