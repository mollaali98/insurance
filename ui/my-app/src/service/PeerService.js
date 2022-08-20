import axios from 'axios'
import authHeader from "./AuthHeader";

const API_GET_INSURER_URL = 'http://localhost:8080/insurer/peers'
const API_GET_INSUREE_URL = 'http://localhost:8080/insuree/peers'

const getPeersInsurer = () => axios.get(API_GET_INSURER_URL, {headers: authHeader()})
const getPeersInsuree = () => axios.get(API_GET_INSUREE_URL, {headers: authHeader()})


const PeerService = {
    getPeersInsurer,
    getPeersInsuree
}

export default PeerService