package com.example.codigosms_usuario.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codigosms_usuario.dto.UsuarioRequest;
import com.example.codigosms_usuario.dto.UsuarioResponse;
import com.example.codigosms_usuario.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest dto){
        return ResponseEntity.status(201).body(service.crearUsuario(dto));
    }

@GetMapping("/{id}")
public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
    UsuarioResponse response = service.obtenerUsuarioPorId(id);
    return ResponseEntity.ok(response);
}

  @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> obtenerTodo(Pageable pageable){
        Page<UsuarioResponse> v = service.listarUsuarios(pageable);
        return ResponseEntity.ok(v);
    
    }
    @GetMapping("/usuario/{nombreUsuario}")
    public ResponseEntity<UsuarioResponse> obtenerPorNombreUsuario(@PathVariable String nombreUsuario) {
        return service.buscarPorNombre(nombreUsuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
    
}
