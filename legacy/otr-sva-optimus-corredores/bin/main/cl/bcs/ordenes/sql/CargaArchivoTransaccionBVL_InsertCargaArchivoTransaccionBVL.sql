INSERT INTO "BSCL.Orden.BD::ORD.CargaTransaccionBVL"
("IdCargaTransaccionBVL","TipoLibro","TipoRepo","TipoBlock","Segmento","Categoria","FolioTransaccion","ClientOrderID","OriginalClientOrderID","TradeId","AgenciaResponsable","AgenciaEnrutador",
"UsuarioEnrutador","AgenciaSponsor","IdEjecucion","IdReferenciaEjecucion","TipoEjecucion","EstadoPropuesta","MotivoRechazo","FechaOperacion","FechaHoraUltimoEvento","FechaLiquidacion",
"Nemotecnico","ISIN","TipoSecurityID","BolsaOrigenInstrumento","TipoOperacion","TipoPropuesta","CantidadPropuesta","CantidadVigente","CantidadAcumulada","CantidadOperacion",
"CantidadVisible","Precio","PrecioOperacion","PrecioPromedio","VigenciaPropuesta","PrecioPropuestaStop","HoraExpiracion","TipoPrecio","Rendimiento","Texto","MonedaInstrumento",
"MonedaLiquidacion","PlazoLiquidacion","AgenciaCalce1","AgenciaCalce2","AgenciaCalce3","AgenciaCalce4","AgenciaCalce5","TasaInteres","Plazo","Garantia1","Garantia2","Garantia3",
"Garantia4","Garantia5","Garantia6","Garantia7","Garantia8","InteresCorrido1","InteresCorrido2","LiquidacionAnticipada","TasaLiquidacionAnticipada","OperacionReferencia",
"AgenciaContraparte","Referencia","IdPropuestaExchange","IdEstado","Resultado")
VALUES("BSCL.Orden.BD.Secuencias::CargaTransaccionBVL".NEXTVAL,:TipoLibro,:TipoRepo,:TipoBlock,:Segmento,:Categoria,:FolioTransaccion,:ClientOrderID,:OriginalClientOrderID,:TradeId,:AgenciaResponsable,:AgenciaEnrutador,
:UsuarioEnrutador,:AgenciaSponsor,:IdEjecucion,:IdReferenciaEjecucion,:TipoEjecucion,:EstadoPropuesta,:MotivoRechazo,:FechaOperacion,:FechaHoraUltimoEvento,:FechaLiquidacion,
:Nemotecnico,:ISIN,:TipoSecurityID,:BolsaOrigenInstrumento,:TipoOperacion,:TipoPropuesta,:CantidadPropuesta,:CantidadVigente,:CantidadAcumulada,:CantidadOperacion,
:CantidadVisible,:Precio,:PrecioOperacion,:PrecioPromedio,:VigenciaPropuesta,:PrecioPropuestaStop,:HoraExpiracion,:TipoPrecio,:Rendimiento,:Texto,:MonedaInstrumento,
:MonedaLiquidacion,:PlazoLiquidacion,:AgenciaCalce1,:AgenciaCalce2,:AgenciaCalce3,:AgenciaCalce4,:AgenciaCalce5,:TasaInteres,:Plazo,:Garantia1,:Garantia2,:Garantia3,
:Garantia4,:Garantia5,:Garantia6,:Garantia7,:Garantia8,:InteresCorrido1,:InteresCorrido2,:LiquidacionAnticipada,:TasaLiquidacionAnticipada,:OperacionReferencia,
:AgenciaContraparte,:Referencia,:IdPropuestaExchange,:IdEstado,:Resultado)