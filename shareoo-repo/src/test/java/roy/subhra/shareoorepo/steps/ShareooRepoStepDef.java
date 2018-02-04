package roy.subhra.shareoorepo.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.DataTable;
import cucumber.api.java8.En;
import cucumber.runtime.CucumberException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import roy.subhra.shareooeureka.ShareooEurekaApplication;
import roy.subhra.shareoorepo.ShareooRepoApplication;
import roy.subhra.shareoorepo.controllers.ShareooRepoController;
import roy.subhra.shareoorepo.model.ShareGroup;
import roy.subhra.shareoorepo.model.User;
import roy.subhra.shareoorepo.repo.ShareGroupRepository;
import roy.subhra.shareoorepo.repo.UserRepository;
import roy.subhra.shareooservices.ShareooServicesApplication;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ShareooEurekaApplication.class,ShareooRepoApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {ShareooRepoApplication.class}, loader = SpringBootContextLoader.class)
@AutoConfigureWebMvc
@AutoConfigureMockMvc
public class ShareooRepoStepDef implements En{

    @Autowired
    private volatile WebApplicationContext webApplicationContext;


    private ResultActions resultActions;

    @LocalServerPort
    int randomServerPort;

    @Autowired
    UserRepository userRepo;
    @Autowired
    ShareGroupRepository groupRepo;
    @Autowired
    private MockMvc mockMvc;

    public ShareooRepoStepDef() {
        Given("^Following Users Exist$", (DataTable table) -> {
            //this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
            List<User> users = table.asList(User.class);
            userRepo.deleteAll();
            groupRepo.deleteAll();
            userRepo.saveAll(users);
        });
        And("^Following Share Groups Exist$", (DataTable table) -> {
            List<ShareGroup> groups = table.asList(ShareGroup.class);
            groupRepo.deleteAll();
            groupRepo.saveAll(groups);
        });

        When("^User calls GET on path:(.*)$", (String path) -> {
            try {
                resultActions = this.mockMvc.perform(get(getUrl(path)).accept(MediaType.APPLICATION_JSON));
                }catch(Exception e){
                    throw new CucumberException(e);
                }
        });

        Then("^Response status code is (\\d+)$", ( statusCode) -> {
            // Write code here that turns the phrase above into concrete actions
            try {
                resultActions.andExpect(status().is(Integer.valueOf((String) statusCode)));
            }catch(Exception e){
                throw new CucumberException(e);
            }
        });
        And("^Response is (.*)$", (String  json) -> {
            try {
                resultActions.andExpect(content().string(json));
            } catch (Exception e) {
                throw new CucumberException(e);
            }
        });
        When("^User calls POST on path:(.*) with (.*)$", (String url, String data) -> {
            try {
                resultActions = this.mockMvc.perform(post(getUrl(url)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(data.getBytes()));
            } catch (Exception e) {
                throw new CucumberException(e);
            }
        });

        When("^User calls POST without Data on path:(.*)$", (String url) -> {
            try {
                resultActions = this.mockMvc.perform(post(getUrl(url)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                throw new CucumberException(e);
            }
        });
        Given("^ShareGroup is changed$", (DataTable table) -> {
            List<String> jsonList = table.asList(String.class);
            ObjectMapper mapper = new ObjectMapper();
            List<ShareGroup> groups = jsonList.stream().map(json->{
                try {
                    return mapper.readValue(json , ShareGroup.class);
                } catch (IOException e) {
                    throw new CucumberException(e);
                }
            }).collect(Collectors.toList());

            groupRepo.saveAll(groups);
        });
        And("^Liabilities for path:(.*) equals (.*)$", (String path, String liabilityJson) -> {
            try {
                ResultActions resultAck = this.mockMvc.perform(get(getUrl(path)).accept(MediaType.APPLICATION_JSON));
                resultAck.andExpect(jsonPath("$._embedded.content.liabilities").value(liabilityJson));
            } catch (Exception e) {
                throw new CucumberException(e);
            }
        });
    }
    private String getUrl(String path){
        return "http://localhost:"+randomServerPort+"/"+webApplicationContext.getServletContext().getContextPath()+path;
    }
}