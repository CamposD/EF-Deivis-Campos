package cibertec.com.pe.controller;

import cibertec.com.pe.model.Ticket;
import cibertec.com.pe.model.Ciudad;
import cibertec.com.pe.model.Venta;
import cibertec.com.pe.model.VentaDetalle;
import cibertec.com.pe.repository.ICiudadRepository;
import cibertec.com.pe.repository.IVentaDetalleRepository;
import cibertec.com.pe.repository.IVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
@SessionAttributes({"boletosAgregados"})
public class VentaBoletosController {

    @Autowired
    private ICiudadRepository ciudadRepository;

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    @GetMapping("/index")
    public String inicioSlash(Model model) {
        List<Ciudad> ciudades = ciudadRepository.findAll();
        List<Ticket> tickets = (List<Ticket>) model.getAttribute("boletosAgregados");

        if(tickets.size()>0){
            Ticket boletoEncontrado = tickets.get(tickets.size()-1);
            model.addAttribute("boleto", boletoEncontrado);
        }else{
            model.addAttribute("boleto", new Ticket());
        }

        model.addAttribute("ciudades", ciudades);


        return "index";
    }

    @GetMapping("/volver-compra")
    public String volverCompra(Model model) {
        List<Ciudad> ciudades = ciudadRepository.findAll();

        model.addAttribute("boleto", new Ticket());
        model.addAttribute("ciudades", ciudades);
        model.addAttribute("boletosAgregados", new ArrayList<>());

        return "index";
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        List<Ciudad> ciudades = ciudadRepository.findAll();
        List<Ticket> tickets = (List<Ticket>) model.getAttribute("boletosAgregados");

        if(tickets.size()>0){
            Ticket boletoEncontrado = tickets.get(tickets.size()-1);
            model.addAttribute("boleto", boletoEncontrado);
        }else{
            model.addAttribute("boleto", new Ticket());
        }

        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @PostMapping("/agregar-boleto")
    public String agregarBoleto(Model model, @ModelAttribute Ticket ticket) {
        List<Ciudad> ciudades = ciudadRepository.findAll();
        List<Ticket> tickets = (List<Ticket>) model.getAttribute("boletosAgregados");
        Double precioBoleto = 50.00;

        ticket.setSubTotal(ticket.getCantidad() * precioBoleto);

        tickets.add(ticket);

        model.addAttribute("boletosAgregados", tickets);
        model.addAttribute("ciudades", ciudades);
        
        model.addAttribute("mensaje", "Boleto agregado correctamente.");
        
        
        model.addAttribute("boleto", new Ticket());

        return "redirect:/index";
    }

    @GetMapping("/comprar")
    public String comprar(Model model) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        List<Ticket> tickets = (List<Ticket>) model.getAttribute("boletosAgregados");
        Double montoTotal = 0.0;

        for (Ticket ticket : tickets) {
            montoTotal += ticket.getSubTotal();
        }

        Venta nuevaVenta = new Venta();
        nuevaVenta.setFechaVenta(new Date());
        nuevaVenta.setMontoTotal(montoTotal);
        nuevaVenta.setNombreComprador(tickets.get(0).getNombreComprador());

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        for (Ticket ticket : tickets) {
            VentaDetalle ventaDetalle = new VentaDetalle();

            Ciudad ciudadDestino = ciudadRepository.findById(ticket.getCiudadDestino()).get();
            ventaDetalle.setCiudadDestino(ciudadDestino);
            Ciudad ciudadOrigen = ciudadRepository.findById(ticket.getCiudadOrigen()).get();
            ventaDetalle.setCiudadOrigen(ciudadOrigen);

            ventaDetalle.setCantidad(ticket.getCantidad());
            ventaDetalle.setSubTotal(ticket.getSubTotal());

            Date fechaRetorno = formatter.parse(ticket.getFechaRetorno());
            ventaDetalle.setFechaRetorno(fechaRetorno);

            Date fechaSalida = formatter.parse(ticket.getFechaSalida());
            ventaDetalle.setFechaViaje(fechaSalida);

            ventaDetalle.setVenta(ventaGuardada);

            ventaDetalleRepository.save(ventaDetalle);
        }

        return "redirect:confirmar";
    }

    @GetMapping("/limpiar")
    public String limpiar(Model model){
        List<Ciudad> ciudades = ciudadRepository.findAll();

        model.addAttribute("boleto", new Ticket());
        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @ModelAttribute("boletosAgregados")
    public List<Ticket> boletosComprados() {
        return new ArrayList<>();
    }
}
