SELECT 
	 CE."IdPersona" 
	,CE."IdDireccionElectronica"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE
WHERE CE."IdEmpleado" = :IdEmpleado;