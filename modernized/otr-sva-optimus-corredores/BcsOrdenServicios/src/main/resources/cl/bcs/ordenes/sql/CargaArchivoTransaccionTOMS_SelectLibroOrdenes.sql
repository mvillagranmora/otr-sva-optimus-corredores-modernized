select "IdLibroOrdenes","IdEmpresa","NombreLibroOrdenes" 
from "BSCL.Orden.BD::ORD.LibroOrdenes"
WHERE "IdEmpresa"=:IdEmpresa AND "LibroPorDefecto" = 'S';