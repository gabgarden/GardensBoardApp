

// package br.edu.iff.ccc.bsi.gardensBoard.filter;
// import java.io.IOException;
// import java.util.Base64;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;
// import at.favre.lib.crypto.bcrypt.BCrypt;
// import br.edu.iff.ccc.bsi.gardensBoard.repository.UserRepository;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class TaskAuthFilter extends OncePerRequestFilter {


//     @Autowired
//     private UserRepository userRepository; 

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//             throws ServletException, IOException {

//             var servletPath = request.getServletPath();

//             if(servletPath.startsWith("/tasks") ) {
                
//             // PEGAR A AUTENTICAÇÃO
//             var authorization = request.getHeader("Authorization");
//             var authEncoded = authorization.substring("Basic".length()).trim();
//             byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
//             String authDecodedString = new String(authDecoded);
//             // OBTEM O NOME E A SENHA DO USUÁRIO
//             String[] credentials =  authDecodedString.split(":");
//             String username = credentials[0];
//             String password = credentials[1];



//             // VALIDAR USUÁRIO
//             var user = userRepository.findByUsername(username);
//             if (user == null) {
//                 response.sendError(401);
//             }else {
//                 // VALIDA A SENHA
//                 var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());
//                 if(passwordVerify.verified){
//                     request.setAttribute("idUser", user.getId());
//                     filterChain.doFilter(request, response);
//                 } else {
//                     response.sendError(401);
//                 }

            

//             }

//             } else {
//                 filterChain.doFilter(request, response);
//             }
            



            
//     }

   

// }
