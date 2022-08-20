import './App.css';
import PeerComponent from '../src/component/PeerComponent'
import HomeComponent from '../src/component/HomeComponent'
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom'
import NavbarComponent from "./component/NavbarComponent";
import InsuranceComponent from "./component/insurance/InsuranceComponent";
import InsurancesComponent from "./component/insurance/InsurancesComponent";
import ClaimComponent from "./component/claim/ClaimComponent";
import ClaimsComponent from "./component/claim/ClaimsComponent";
import Login from "./component/auth/Login";
import Register from "./component/auth/Register";
import Profile from "./component/auth/Profile";


function App() {
    return (
        <div className="App">
            <NavbarComponent/>
            <Router>
                <Routes>
                    <Route exact path="/" element={<HomeComponent/>}/>
                    <Route exact path="/login" element={<Login/>}/>
                    <Route exact path="/register" element={<Register/>}/>
                    <Route exact path="/profile" element={<Profile/>}/>
                    <Route exact path="/peers" element={<PeerComponent/>}/>
                    <Route exact path="/insurance" element={<InsuranceComponent/>}/>
                    <Route exact path="/insurances" element={<InsurancesComponent/>}/>
                    <Route exact path='/insurance/:id/claim' element={<ClaimComponent/>}/>
                    <Route exact path='/insurance/:id/claims' element={<ClaimsComponent/>}/>
                    <Route exact path='/insurance/:id/claims' element={<ClaimsComponent/>}/>
                </Routes>
            </Router>
        </div>
    );
}

export default App;
