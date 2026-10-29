insert into "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_Log"
("IdTrabajo","IdCabeceraLog","IdTipoLog","IdModulo","IdUsuario","IdEmpresa","FechaSistemaLog","IdUsuarioAtendido",
"IdCliente","EstadoCliente","NombreCliente","IdTipoDocumentoIdentidad","IdDocumentoIdentidad","IdPortafolio",
"EstadoPortafolio","IdTipoRegistro","IdRegistro","EstadoRegistro","IdTipoOperacion","Folio","Cantidad",
"PrecioTasa","Monto","IdMonedaOrigen","IdMonedaLiquidacion","TipoCambio","Nemo","IdProceso","EstadoProceso",
"NombreProceso","IdPersona")
values (:IdTrabajo,:IdCabeceraLog,:IdTipoLog,:IdModulo,:IdUsuario,:IdEmpresa,:FechaSistemaLog,:IdUsuarioAtendido,
:IdCliente,:EstadoCliente,:NombreCliente,:IdTipoDocumentoIdentidad,:IdDocumentoIdentidad,:IdPortafolio,
:EstadoPortafolio,:IdTipoRegistro,:IdRegistro,:EstadoRegistro,:IdTipoOperacion,:Folio,:Cantidad,:PrecioTasa,
:Monto,:IdMonedaOrigen,:IdMonedaLiquidacion,:TipoCambio,:Nemo,:IdProceso,:EstadoProceso,:NombreProceso,:IdPersona);
