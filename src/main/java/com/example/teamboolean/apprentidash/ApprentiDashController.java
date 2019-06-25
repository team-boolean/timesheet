package com.example.teamboolean.apprentidash;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.constraints.Null;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.security.Principal;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;

@Controller
public class ApprentiDashController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DayRepository dayRepository;

    Day currentDay = new Day();

    @GetMapping("/")
    public String getHome(Model m, Principal p){

        //Check if the user is logged in and pass the user info to the model
        boolean isLoggedIn;
        String currentUserFirstName;
        if(p == null){
            isLoggedIn = false;
            currentUserFirstName = "Visitor";
        }else {
            isLoggedIn = true;
            currentUserFirstName = userRepository.findByUsername(p.getName()).getFirstName();
        }
        m.addAttribute("isLoggedIn", isLoggedIn);
        m.addAttribute("userFirstName", currentUserFirstName);

        return "home";
    }

    @GetMapping("/login")
    public String getLogin(){
        return "login";
    }

    @GetMapping("/signup")
    public String startSignUp(){
        return "signup";
    }

    @PostMapping("/signup")
    public String addUser(String username, String password, String firstName, String lastName, String managerName){
        AppUser newUser = new AppUser(username, passwordEncoder.encode(password), firstName, lastName, managerName);
        userRepository.save(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(newUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return "redirect:/";
    }


    //****** The controller methods to handle our Punch In page ******/
    @GetMapping("/recordHour")
    public String recordHour(Model m){
        m.addAttribute("workStatus", buttonRenderHelper());
        return "recordHour";
    }


//Route to handle our clock in button
    @PostMapping(value="/recordHour", params="name=value")
    public String clockInSave(Principal p, Model m) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        System.out.println(buttonRenderHelper());
        boolean endOfDay = false;
        if(buttonRenderHelper().equals("clockIn")) {
            currentDay.setClockIn(now);
        }else if(buttonRenderHelper().equals(("lunchIn"))) {
            currentDay.setLunchStart(now);
        }else if(buttonRenderHelper().equals("lunchOut")) {
            currentDay.setLunchEnd(now);
        }else if(buttonRenderHelper().equals("clockOut")){
            System.out.println("I got in here");
            currentDay.setClockOut(now);
            endOfDay = true;
        }
        currentDay.setUser(userRepository.findByUsername(p.getName()));
        dayRepository.save(currentDay);
        if(endOfDay){
            currentDay = new Day();
        }

        m.addAttribute("workStatus", buttonRenderHelper());
        return "redirect:/recordHour";
    }

    public String buttonRenderHelper(){
        if(currentDay.getClockIn() == null)
            return "clockIn";
        else if(currentDay.getLunchStart() == null)
            return "lunchIn";
        else if(currentDay.getLunchEnd() == null)
            return "lunchOut";
        else if(currentDay.getClockOut() == null)
            return "clockOut";
        return null;
    }

//**************** End of the controller for handle Punch In page *************************//


    @GetMapping("/summary")
    public String getSummary(Principal p, Model m){
        AppUser currentUser = userRepository.findByUsername(p.getName());
        m.addAttribute("localDate", LocalDate.now());
        m.addAttribute("user", currentUser);
        return "summary";
    }

}
