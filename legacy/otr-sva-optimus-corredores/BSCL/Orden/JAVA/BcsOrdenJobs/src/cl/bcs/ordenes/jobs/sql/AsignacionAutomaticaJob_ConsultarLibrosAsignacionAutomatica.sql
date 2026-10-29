/********************************************************** 
Proposito	: Script para obtener libros con asignacion automatica
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT "IdLibroOrdenes","NombreLibroOrdenes","IdTipoAsignacion","IdEstado" 
FROM "BSCL.Orden.BD::ORD.LibroOrdenes"
where "IdEstado" = 'ABT' and "AsignacionAutomatica" = 'S' and "IdEmpresa" = :p_IdEmpresa
